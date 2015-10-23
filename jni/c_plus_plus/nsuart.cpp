/*
 * nsuart.cpp
 *  JNI interface driver for UART to communicate with BMD101
 *
 *	BMD101 uses 57600 baudrate with 8 data bits and one stop bit. There is no parity
 */
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <termios.h>
#include <errno.h>
#include <string.h>

#include <android/log.h>
#define LOG_TAG "nsuart"

#undef LOG

#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)

#include "jni.h"

int speed_arr[]={B115200, B57600,B38400, B19200, B9600, B4800, B2400, B1200, B300,};
int name_arr[]={115200,57600,38400,19200, 9600, 4800, 2400, 1200, 300,};
int fd;

/*
 * Serial port Baud rate Settings
 */
static void
JNI_set_1speed(JNIEnv *env, jobject obj, jint fd, jint speed)
{
    int i;
    int status;
    struct termios Opt;
    tcgetattr(fd, &Opt);
    for(i=0; i<sizeof(speed_arr)/sizeof(int); i++)
    {
        if(speed == name_arr[i])
        {
            tcflush(fd, TCIOFLUSH);
            cfsetispeed(&Opt, speed_arr[i]);
            cfsetospeed(&Opt, speed_arr[i]);
            status = tcsetattr(fd, TCSANOW, &Opt);
            if(status != 0)
                perror("tcsetattr fd1\n");
            return ;
        }
        tcflush(fd, TCIOFLUSH);
    }
}
/*
 * Serial port Settings
 */
static jint
SerialJNI_set_1Parity(JNIEnv *env, jobject obj, jint fd, jint databits, jint stopbits, jint parity)
{
    struct termios opt;
    /*
     * get terminal attribute, then stored in an instance of the struct termios 'opt'
     */
    if(tcgetattr(fd, &opt) != 0)
    {
        perror("SetupSerial 1\n");
        return -1;
    }
    /*
     * 	"control flags (bitmask)"
     *		Turn off character processing
     *  	clear current char size mask, no parity checking,
     *  	no output processing, force 8 bit input
     */
    opt.c_cflag &= ~CSIZE;
    /*
     *  "local flags (bitmask)"
     *		No line processing:
     * 		echo off, echo newline off, canonical mode off,
     * 		extended input processing off, signal chars off
     */
    opt.c_lflag &= ~(ICANON|ECHO|ECHOE|ISIG); // set for Raw data

    /*
     * 	"output specific flags (bitmask)"
     * 		Output flags - Turn off output processing ,
     * 		no CR to NL translation, no NL to CR-NL translation,
     * 		no NL to CR translation, no column 0 CR suppression,
     * 		no Ctrl-D suppression, no fill characters, no case mapping,
     * 		no local output processing
     */
    opt.c_oflag &= ~OPOST;

    /*
     * set char size 'CS7' or 'CS8' mask
     */
    switch(databits)
    {
        case 7: opt.c_cflag |= CS7; break;
        case 8: opt.c_cflag |= CS8; break;
        default: fprintf(stderr, "Unsupported data size\n");
             return -1;
    }
    /*
     *  set parity checking
     */
    switch(parity)
    {
        case 'n':
        case 'N': opt.c_cflag &= ~PARENB;
              opt.c_iflag &= ~INPCK;
              break;
        case 'o':
        case 'O': opt.c_cflag |= (PARODD|PARENB);
              opt.c_iflag |= INPCK;
              break;
        case 'e':
        case 'E': opt.c_cflag |= PARENB;
              opt.c_cflag &= ~PARODD;
              opt.c_iflag |= INPCK;
              break;
        case 's':
        case 'S': opt.c_cflag &= ~PARENB;
              opt.c_cflag &= ~CSTOPB;
              break;
        default: fprintf(stderr, "Unsupported parity\n");
             return -1;

    }
    /*
     * set stop bits
     */
    switch(stopbits)
    {
        case 1: opt.c_cflag &= ~CSTOPB;
                break;
        case 2: opt.c_cflag |= CSTOPB;
            	break;
        default: fprintf(stderr,"Unsupported stop bits\n");
             	 return -1;
    }

    if (parity != 'n')  opt.c_iflag |= INPCK;

    /*
     * 	"special characters"
     *  	One input byte is enough to return from read()
     *   	Inter-character timer off
     */
    opt.c_cc[VTIME] = 150;
    opt.c_cc[VMIN] = 0;
    tcflush(fd, TCIFLUSH);
    if (tcsetattr(fd,TCSANOW,&opt) != 0)
    {
        perror("SetupSerial 3\n");
        return -1;
    }
     return 0;
}
/*
 * Serial port Open
 */

static jobject
SerialJNI_open(JNIEnv *env, jobject obj, jstring Dev)
{
//	__android_log_print(ANDROID_LOG_INFO,LOG_TAG,__FILE__ );
	LOGI("beginning to open uart serial port \n");
	LOGI("%s ---> %s info\n", __FILE__,__func__);

    const char *Dev_utf = env->GetStringUTFChars(Dev, JNI_FALSE);
    fd = open(Dev_utf, O_RDONLY | O_NOCTTY | O_NDELAY);

    //set Baud rate to 57600 to talk to BMD101
    JNI_set_1speed(env, obj, fd, 57600);
    //set 8 data bits, no parity and one stop bit
    SerialJNI_set_1Parity(env,obj,fd,8,1,'n');
    if(-1 == fd)
    {
        perror("Can't Open Serial Port\n");
        return NULL;
    } else {
    	env->ReleaseStringUTFChars(Dev, Dev_utf);
        printf("Serial Open\n");
		jobject mFileDescriptor;
		jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
		jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
		jfieldID descriptorID = env->GetFieldID( cFileDescriptor, "descriptor", "I");
		mFileDescriptor = env->NewObject( cFileDescriptor, iFileDescriptor);
		env->SetIntField( mFileDescriptor, descriptorID, (jint)fd);
		return mFileDescriptor;
	}
}
/*
 * Serial port close
 */
static void
SerialJNI_close (JNIEnv *env, jobject obj)
{
	if(close(fd))
	    {
	        perror("close failed!\n");
	    }
	    else
	    {
	        printf("close success!\n");
	    }
	fd = 0;
}


static const char *classPathName = "com/neurosky/connection/Native";

static JNINativeMethod methods[] = {
  {"SerialJNI_open","(Ljava/lang/String;)Ljava/io/FileDescriptor;", (void*)SerialJNI_open },
  {"SerialJNI_close","()V",(void*)SerialJNI_close},
};

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 *
 * returns JNI_TRUE on success.
 */
static int registerNatives(JNIEnv* env)
{
  if (!registerNativeMethods(env, classPathName,
                 methods, sizeof(methods) / sizeof(methods[0]))) {
    return JNI_FALSE;
  }

  return JNI_TRUE;
}


typedef union {
    JNIEnv* env;
    void* venv;
} UnionJNIEnvToVoid;


/*
 * This is called by the VM when the shared library is first loaded.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    UnionJNIEnvToVoid uenv;
    uenv.venv = NULL;
    jint result = -1;
    JNIEnv* env = NULL;


    if (vm->GetEnv(&uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        goto bail;
    }
    env = uenv.env;

    if (registerNatives(env) != JNI_TRUE) {
        goto bail;
    }

    result = JNI_VERSION_1_4;

bail:
    return result;
}
