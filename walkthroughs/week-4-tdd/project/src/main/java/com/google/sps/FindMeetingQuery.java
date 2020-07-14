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
    Collection<String> requestAttendees = request.getAttendees();
    List<TimeRange> res = new LinkedList<>();
    res.add(TimeRange.WHOLE_DAY);
    for (Event e : events) {
      Set<String> Attendees = e.getAttendees();
      boolean flag = false;
      for (String a : Attendees) {
        if (requestAttendees.contains(a)) {
          flag = true;
          break;
        }
      }
      if (!flag){
        continue;
      }
      TimeRange badTime = e.getWhen();
      ListIterator it = res.listIterator();
      while (it.hasNext()) {
        TimeRange goodTime = (TimeRange) it.next();
        if (goodTime.start() >= badTime.start() + badTime.duration())
          continue;
        if (goodTime.start() < badTime.start()) {
          if (badTime.start() >= goodTime.start() + goodTime.duration()) {
            continue;
          } else if (badTime.start() + badTime.duration() >= goodTime.start() + goodTime.duration()) {
            it.set(TimeRange.fromStartDuration(goodTime.start(), badTime.start() - goodTime.start()));
          } else {
            it.set(TimeRange.fromStartDuration(goodTime.start(), badTime.start() - goodTime.start()));
            it.add(TimeRange.fromStartDuration(badTime.start() + badTime.duration(),
                goodTime.start() + goodTime.duration() - badTime.start() - badTime.duration()));
          }
        } else {
          if (badTime.start() + badTime.duration() >= goodTime.start() + goodTime.duration()) {
            it.remove();
          } else {
            it.set(TimeRange.fromStartDuration(badTime.start() + badTime.duration(),
                goodTime.start() + goodTime.duration() - badTime.start() - badTime.duration()));
          }
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
