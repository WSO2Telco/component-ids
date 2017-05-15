import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Observable';
import {AppSettings} from "../../../app.settings";


@Injectable()
export class FeedService {

  constructor(private http:Http) {
  }

  getFeeds() {
    let token = localStorage.getItem('access_token');
    return this.http.get(AppSettings.BASE_API + 'user/login_history?access_token='+ token).map((res:Response) => res.json());
  }

  /* getData():Observable<Feed[]> {
   return this.http.get('http://jsonplaceholder.typicode.com/comments')
   .map(this.extractData)
   .catch(this.handleError);
   } */

  private extractData(res:Response) {
    let body = res.json();
    return body || [];
  }

  private handleError(error:any) {
    // In a real world app, we might use a remote logging infrastructure
    // We'd also dig deeper into the error to get a better message
    let errMsg = (error.message) ? error.message :
        error.status ? `${error.status} - ${error.statusText}` : 'Server error';
    console.error(errMsg); // log to console instead
    return Observable.throw(errMsg);

  }


}