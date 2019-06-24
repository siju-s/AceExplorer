#include <jni.h>
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
const char *KEY = "TPP[PsXW[~rhqrp^ n)[XH\\_XXVZXH!XTPP[Z~RZXH\\Xvt^[hp)}^q`\\(RiqoMaz/R*VAjMjN\\zX}UWj~++Lw6/OSxrpxstCT[V]rmK~~QuL~N]CCo_CZn+jv,*L22iOQK`|oRP{I.6|Pr[.tmuxkmj{Vr]*`^HZLOa\\(rH*Vult(Z@o.]h[HZ-S q h++Xiz^PrCh/Vj*St.oRtlcQQURW/*`NHJ(_lnnzUXtiJW+\\VA-Qxj-|\\uk~Cv`Jl-ho,JVvvVSJ+.@-ccaMvHA,M,)mV }^2W@RkUtIR-`U,S^[,\\*L])P!oWU]6Ns+hI[\\(mp@{sQQ|A*Ik_ uSqAmCj lpTwTcva/}aN 2OtI@aWlTAxrAkLK^i~xN^R))CHP]XHX[";
unsigned char NUM = 25;
char output[] = "";

void xor_value_with_key(const char* value, char* xorOutput){
    int i = 0;
    while(value[i] != '\0'){
        xorOutput[i] = value[i] ^ NUM;
        i++;
    }
}

jstring Java_com_siju_billingsecure_BillingKey_getNativeKey(JNIEnv *env, jobject thiz) {

    xor_value_with_key(KEY, output);

    return (*env)->NewStringUTF(env, output);
}




#pragma clang diagnostic pop