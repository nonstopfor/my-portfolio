// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Given a set of existing events, a new request should be arranged at right time
    // the restriction is that the attendees in the new request should be present at one meeting at a time
    // this function returns a collection of appropriate time for the new request
    Collection<String> requestAttendees = request.getAttendees();
    List<TimeRange> res = new LinkedList<>();
    res.add(TimeRange.WHOLE_DAY);
    for (Event event : events) {
      Set<String> Attendees = event.getAttendees();
      boolean flag = false;
      for (String attendee : Attendees) {
        if (requestAttendees.contains(attendee)) {
          flag = true;
          break;
        }
      }
      if (!flag) {
        continue;
      }
      TimeRange badTime = event.getWhen();
      ListIterator it = res.listIterator();
      while (it.hasNext()) {
        TimeRange goodTime = (TimeRange) it.next();

        if (!goodTime.overlaps(badTime)) {
          continue;
        }
        if (goodTime.contains(badTime)) {
          it.remove();
          it.add(TimeRange.fromStartDuration(goodTime.start(), badTime.start() - goodTime.start()));
          it.add(TimeRange.fromStartDuration(badTime.end(), goodTime.end() - badTime.end()));
        } else if (badTime.contains(goodTime)) {
          it.remove();
        } else if (goodTime.contains(badTime.start())) {
          it.set(TimeRange.fromStartDuration(goodTime.start(), badTime.start() - goodTime.start()));
        } else {
          it.set(TimeRange.fromStartDuration(badTime.end(), goodTime.end() - badTime.end()));
        }
      }
    }
    ListIterator it = res.listIterator();
    while (it.hasNext()) {
      TimeRange goodTime = (TimeRange) it.next();
      if (goodTime.duration() < request.getDuration()) {
        it.remove();
      }
    }
    return res;
  }
}
