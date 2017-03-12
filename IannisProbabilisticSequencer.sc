IannisProbabilisticSequencer {
  var <name, <synthName, <length, <seed, 
  <>root, <>scale;

  *new {arg name, synthName, length;
    ^super.new.init(name, synthName, length);
  }

  init {arg sequencerName, correspondingSynthName, patternLength;
    name = sequencerName;
    synthName = correspondingSynthName;
    length = patternLength;

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

    // if newValues contains values and weights contains non-zero(s)
    // -- apply it
    if(newValues.size > 0 && newWeights.indexOfGreaterThan(0).notNil, {
      Pbindef(name, key, Pwrand(newValues, newWeights, inf)*(mul?1)+(add?0));
    }, {
      if(key == \dur, {
        // if there is no values for duration -- apply default
        // value
        ("There is no values for duration. Applying default duration.").inform;

        Pbindef(name, key, 1*(mul?1)+(add?0));
      }, {
        // otherwise just print that there is no values
        ("There is no values or weights are 0s for key:"+key.asString).inform;
      });
    });
  }

  changeLength {arg newLength;
    var quant = Pdef((name++"_repeater").asSymbol).quant;
    Pdef((name++"_repeater").asSymbol).quant = length;

    length = newLength;

    Pdef((name++"_repeater").asSymbol, Pn(Pfindur(newLength, Pseed(seed, Pbindef(name))), inf));

    Pdef((name++"_repeater").asSymbol).quant = quant;
  }

  seed_ {arg seed;
    seed = seed;
    Pdef((name++"_repeater").asSymbol, Pn(Pfindur(length, Pseed(seed, Pbindef(name))), inf));
  }

  regenerate {
    seed = 2147483647.rand;
    Pdef((name++"_repeater").asSymbol, Pn(Pfindur(length, Pseed(seed, Pbindef(name))), inf));
  }

  play {
    Pdef((name++"_repeater").asSymbol).play();
  }

  stop {
    Pdef((name++"_repeater").asSymbol).stop();
  }

  reset {
    Pdef((name++"_repeater").asSymbol).reset();
  }
}
