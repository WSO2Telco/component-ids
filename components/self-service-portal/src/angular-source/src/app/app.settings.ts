export class AppSettings {
   public static BASE_API = 'http://localhost:9763/selfserviceportal/api/v1/';

   public static getAuthUrl(msisdn:string, acr:string):string{
       if(acr == null)
           return this.BASE_API + "auth/login?msisdn=" + msisdn;
       else
           return this.BASE_API + "auth/login?acr=" + acr + "&msisdn=" + msisdn;
   }
}
