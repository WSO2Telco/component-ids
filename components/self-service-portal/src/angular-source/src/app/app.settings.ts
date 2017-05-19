export class AppSettings {
   public static BASE_API = 'http://localhost:9763/selfserviceportal/api/v1/';

   public static getAuthUrl(msisdn:string):string{
     return this.BASE_API + "auth/login?msisdn=" + msisdn;
   }
}
