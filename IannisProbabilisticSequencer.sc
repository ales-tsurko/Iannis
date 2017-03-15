IannisProbabilisticSequencer {
  var <name, <synthName, <length, <seed, 
  <>root, <>scale, <time, <>timeAction;

  *new {arg name, synthName, length;
    ^super.new.init(name, synthName, length);
  }

  init {arg sequencerName, correspondingSynthName, patternLength;
    name = sequencerName;
    synthName = correspondingSynthName;
    length = patternLength;
    time = 0;

    // define main pattern
    Pbindef(name, 
      \instrument, synthName, 
      \root, Pfunc({root}, inf),
      \scale, Pfunc({scale}, inf),
      // update time
      // \pfunc, Pif(
        // // do only on whole beats
        // Ptime(inf).frac <= 0.0, 
        // Pif(
          // // if a beat is 0 (the first beat)
          // Ptime(inf) <= 0.0, 
          // Pfunc({
            // AppClock.sched(0.0, {this.time = 1});
          // }), 
          // Pfunc({
            // AppClock.sched(0.0, {this.time = this.time + 1});
          // })
        // ),
        // // in neither case return something
        // 1
      // )
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
    Pn(
      Plazy({
        var dur = length.next??{length.reset;length.next??{length=4;length.next}};
        var version = seed.next??{seed.reset;seed.next??{seed=2147483647.rand;seed.next}};
        Pfindur(dur, Ppar([
          Pseed(version, Pbindef(name)),
          // per beat
          Plazy({
            var ptime = Ptime.new.asStream;
            Pbind(
              \dur, 1,
              \pfunc, Pfunc({
                AppClock.sched(0.0, {this.time = ptime.next.round + 1});
                1;
              });
            )
          })
        ]))
      }), inf)
    );
  }

  time_ {arg newTime;
    time = newTime;
    if(timeAction.notNil, {timeAction.value(newTime)});
  }

  play {
    time = 0;
    Pdef((name++"_repeater").asSymbol).play();
  }

  stop {
    time = 0;
    Pdef((name++"_repeater").asSymbol).stop();
  }

  reset {
    time = 0;
    Pdef((name++"_repeater").asSymbol).reset();
  }
}
