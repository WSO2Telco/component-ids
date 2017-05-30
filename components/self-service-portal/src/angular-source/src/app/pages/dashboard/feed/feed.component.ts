import {Component} from '@angular/core';

import {FeedService} from './feed.service';

import 'style-loader!./feed.scss';

@Component({
  selector: 'feed',
  templateUrl: './feed.html'
})

export class Feed {

  public feed:Array<Object>;
  public feed_error:Boolean = false;

  constructor(private _feedService:FeedService) {
  }

  ngOnInit() {
    this.getFeeds();
  }

  expandMessage (message){
    message.expanded = !message.expanded;
  }

  getFeeds() {
    this._feedService.getFeeds().subscribe(
        data => { this.feed = data},
        err => { this.feed_error = true }
    );

  }

}
