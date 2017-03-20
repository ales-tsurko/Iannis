IannisProbabilisticSequencer {
  var <name, <synthName, <length, <seed, 
  <>root, <>scale, <time, <>timeAction, <playTimes,
  onFinishActions, addOnFinishAction, 
  data;

  *new {arg name, synthName, length;
    ^super.new.init(name, synthName, length);
  }

  init {arg sequencerName, correspondingSynthName, patternLength;
    name = sequencerName;
    synthName = correspondingSynthName;
    length = patternLength;
    time = 0;
    this.playTimes = 0;
    onFinishActions = [];
    data = IdentityDictionary.new;
    data[\mul] = IdentityDictionary.new;
    data[\add] = IdentityDictionary.new;

    // define main pattern
    Pbindef(name, 
      \instrument, synthName, 
      \root, Pfunc({root}, inf),
      \scale, Pfunc({scale}, inf)
    );

    this.regenerate();
  }

  updateEvent {arg key, values, weights, numberOfSteps;
    var newValues = [], newWeights = [];
    var cropedValues, cropedWeights;
    cropedValues = values.keep(numberOfSteps);
    cropedWeights = weights.keep(numberOfSteps);

    // filter values and weights for nil-values
    cropedValues.asList.do({arg item, n;
      // update appropriate weights in parallel
      if(item.notNil, {
        if(key != \dur, {
          newWeights = newWeights.add(cropedWeights[n]);
          newValues = newValues.add(item);
        }, {
          // if key is \dur

          // if item.isNumber && item > 0
          if(item.isNumber, {
            if(item > 0, {
              newWeights = newWeights.add(cropedWeights[n]);
              newValues = newValues.add(item);
            });
          }, {
            // if item.isNumber == false
            newWeights = newWeights.add(cropedWeights[n]);
            newValues = newValues.add(item);
          });
        });
      });
    });

    // normalize values as probabilities
    newWeights = newWeights.normalizeSum;

    // prevent entering 0 for duration
    if(key == \dur, {
      if(data[\mul][key].isNil) {this.setMul(\dur, 1, 1)};
      if(data[\mul][key][\value].isNumber, {
        if(data[\mul][key][\value] <= 0, {
          data[\mul][key][\value] = 1;
        });
      });
    });

    // if newValues contains values and weights contains non-zero(s)
    // -- apply it
    if(newValues.size > 0 && newWeights.indexOfGreaterThan(0).notNil, {
      Pbindef(name, key, 
        Pwrand(newValues, newWeights, inf)
        * Pfunc({data[\mul][key][\current]?1})
        + Pfunc({data[\add][key][\current]?0}));
    }, {
      if(key == \dur, {
        // if there is no values for duration -- apply default
        // value
        ("There is no values for duration. Applying default duration.").inform;
        Pbindef(name, key, 
        1 * Pfunc({data[\mul][key][\current]?1})
        + Pfunc({data[\add][key][\current]?0}));
      }, {
        // otherwise just print that there is no values
        ("There is no values or weights are 0s for key:"+key.asString).inform;
      });
    });
  }

  length_ {arg newLength;
    length = newLength.asStream;
    this.updateRepeater();
  }

  seed_ {arg newSeed;
    seed = newSeed.asStream;
    this.updateRepeater();
  }

  regenerate {
    this.seed = 2147483647.rand;
  }

  updateRepeater {
    Pdef((name++"_repeater").asSymbol, 
    Pseq([
      Pn(
        Plazy({
          var dur = length.next??{length.reset;length.next??{length=4;length.next}};
          var version = seed.next??{seed.reset;seed.next??{seed=2147483647.rand;seed.next}};
          var ptime = Ptime.new.asStream;
          var mulAddEvents = [];

          // filling mul/add events
          [\mul, \add].do({arg topkey;
            data[topkey].keysDo({arg key;
              var playFunc = {
                var stream = data[topkey][key][\value];
                data[topkey][key][\current] = stream.next??{stream.reset;stream.next};
              };
              var dur = data[topkey][key][\dur];
              // add event
              var event = (play: playFunc, dur: dur);
              mulAddEvents = mulAddEvents.add(event);
            });
          });

          // main pattern
          Pfindur(dur, Ppar([
            Pseed(version, Pbindef(name)),
            // beat counter
            (play: {
              AppClock.sched(0.0, {this.time = ptime.next.round + 1});
            }, 
            dur: 1)
          ] ++ mulAddEvents))
        }), playTimes), 

        // do on finished playing pattern
        Pfuncn({
          AppClock.sched(0.0, {
            onFinishActions.do(_.());
            this.stop();
          });
          // should return something
          1;
        });
      ])
    );
  }

  setMul {arg forKey, value, duration;
    if (data[\mul][forKey].isNil) {data[\mul][forKey] = IdentityDictionary.new};
    data[\mul][forKey][\value] = value.asStream;
    data[\mul][forKey][\dur] = duration;

    this.updateRepeater();
  }

  setAdd {arg forKey, value, duration;
    if (data[\add][forKey].isNil) {data[\add][forKey] = IdentityDictionary.new};
    data[\add][forKey][\value] = value.asStream;
    data[\add][forKey][\dur] = duration;

    this.updateRepeater();
  }

  time_ {arg newTime;
    time = newTime;
    if(timeAction.notNil, {timeAction.value(newTime)});
  }

  playTimes_ {arg newValue;
    if (newValue == 0) {
      playTimes = inf;
    } {
      playTimes = newValue;
    };
    this.updateRepeater();
  }

  play {
    this.time = 1;
    Pdef((name++"_repeater").asSymbol).play();
  }

  stop {
    this.time = 1;
    Pdef((name++"_repeater").asSymbol).stop();
    // reset subevents
    [\mul, \add].do({arg topkey;
      data[topkey].keysDo({arg key;
        data[topkey][key][\value].reset();
      });
    });
  }

  reset {
    this.time = 1;
    Pdef((name++"_repeater").asSymbol).reset();
    // reset subevents
    [\mul, \add].do({arg topkey;
      data[topkey].keysDo({arg key;
        data[topkey][key][\value].reset();
      });
    });
  }

  addOnFinishAction {arg action;
    onFinishActions = onFinishActions.add(action);
  }
}
