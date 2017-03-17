IannisProbabilisticSequencer {
  var <name, <synthName, <length, <seed, 
  <>root, <>scale, <time, <>timeAction, <playTimes,
  onFinishActions, addOnFinishAction;

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

    // define main pattern
    Pbindef(name, 
      \instrument, synthName, 
      \root, Pfunc({root}, inf),
      \scale, Pfunc({scale}, inf)
    );

    this.regenerate();
  }

  updateEvent {arg key, values, weights, mul, add, numberOfSteps;
    var newValues = [], newWeights = [];
    var cropedValues, cropedWeights;
    var newMul = mul;
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
      if(mul.isNumber, {
        if(mul <= 0, {
          newMul = 1;
        });
      });
    });

    // if newValues contains values and weights contains non-zero(s)
    // -- apply it
    if(newValues.size > 0 && newWeights.indexOfGreaterThan(0).notNil, {
      Pbindef(name, key, Pwrand(newValues, newWeights, inf)*(newMul?1)+(add?0));
    }, {
      if(key == \dur, {
        // if there is no values for duration -- apply default
        // value
        ("There is no values for duration. Applying default duration.").inform;
        Pbindef(name, key, 1*(newMul?1)+(add?0));
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
          Pfindur(dur, Ppar([
            Pseed(version, Pbindef(name)),
            // per beat event
            (play: {
              AppClock.sched(0.0, {this.time = ptime.next.round + 1});
            }, 
            dur: 1);
          ]))
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
  }

  reset {
    this.time = 1;
    Pdef((name++"_repeater").asSymbol).reset();
  }

  addOnFinishAction {arg action;
    onFinishActions = onFinishActions.add(action);
  }
}
