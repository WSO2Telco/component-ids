export class AppSettings {
   public static BASE_API = 'https://localhost:9443/selfserviceportal/api/v1/';

   public static getAuthUrl(msisdn:string):string{
     return this.BASE_API + "auth/login?msisdn=" + msisdn;
   }
}
