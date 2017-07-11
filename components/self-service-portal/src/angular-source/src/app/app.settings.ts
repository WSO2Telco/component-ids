export class AppSettings {

    public static port = window.location.origin;
    public static BASE_API = AppSettings.port+'/selfserviceportal/api/v1/';

   public static getAuthUrl(msisdn:string, acr:string):string{
       if(acr == null)
           return this.BASE_API + "auth/login?msisdn=" + msisdn;
       else
           return this.BASE_API + "auth/login?acr=" + acr + "&msisdn=" + msisdn;
   }

}
