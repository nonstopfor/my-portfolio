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
import java.util.ListIterator;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<String> Request_attendees = request.getAttendees();
    List<TimeRange> res = new LinkedList<>();
    res.add(TimeRange.WHOLE_DAY);
    for (Event e : events) {
      Set<String> Attendees = e.getAttendees();
      boolean flag = false;
      for (String a : Attendees) {
        if (Request_attendees.contains(a)) {
          flag = true;
          break;
        }
      }
      if (!flag)
        continue;
      TimeRange bad = e.getWhen();
      ListIterator it = res.listIterator();
      while (it.hasNext()) {
        TimeRange t = (TimeRange) it.next();
        if (t.start() >= bad.start() + bad.duration())
          continue;
        if (t.start() < bad.start()) {
          if (bad.start() >= t.start() + t.duration()) {
            continue;
          } else if (bad.start() + bad.duration() >= t.start() + t.duration()) {
            it.set(TimeRange.fromStartDuration(t.start(), bad.start() - t.start()));
          } else {
            it.set(TimeRange.fromStartDuration(t.start(), bad.start() - t.start()));
            it.add(TimeRange.fromStartDuration(bad.start() + bad.duration(),
                t.start() + t.duration() - bad.start() - bad.duration()));
          }
        } else {
          if (bad.start() + bad.duration() >= t.start() + t.duration()) {
            it.remove();
          } else {
            it.set(TimeRange.fromStartDuration(bad.start() + bad.duration(),
                t.start() + t.duration() - bad.start() - bad.duration()));
          }
        }
      }

    }
    ListIterator it = res.listIterator();
    while (it.hasNext()) {
      TimeRange t = (TimeRange) it.next();
      if (t.duration() < request.getDuration()) {
        it.remove();
      }
    }
    return res;
  }
}
