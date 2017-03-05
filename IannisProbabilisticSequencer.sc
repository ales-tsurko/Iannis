IannisProbabilisticSequencer {
	var <name, <synthName, <length, currentSeed;

	*new {arg name, synthName, length;
		^super.new.init(name, synthName, length);
	}

	init {arg sequencerName, correspondingSynthName, patternLength;
		name = sequencerName;
		synthName = correspondingSynthName;
		length = patternLength;

		Pbindef(name, \instrument, synthName);
		this.regenerate();
	}

	updatePitches {arg pitchesArray, weightsArray;
		var pitchesPattern = Pwrand(pitchesArray.asList, weightsArray.normalizeSum, inf);
		Pbindef(name, \midinote, pitchesPattern);
	}

	updateRhythm {arg rhythmWeights;
		var rhythmFigures = [], weights = [];

		rhythmWeights.keysValuesDo({arg key, val;
			switch(key,
				\quarter, {
					rhythmFigures = rhythmFigures.add(Pseq([1]));
					weights = weights.add(val.clip(0, 1));
				},
				\dotedQuarter, {
					rhythmFigures = rhythmFigures.add(Pseq([1.5]));
					weights = weights.add(val.clip(0, 1));
				},
				\half, {
					rhythmFigures = rhythmFigures.add(Pseq([2]));
					weights = weights.add(val.clip(0, 1));
				},
				\dotedHalf, {
					rhythmFigures = rhythmFigures.add(Pseq([3]));
					weights = weights.add(val.clip(0, 1));
				},
				\whole, {
					rhythmFigures = rhythmFigures.add(Pseq([4]));
					weights = weights.add(val.clip(0, 1));
				},
				\eight, {
					rhythmFigures = rhythmFigures.add(Pseq([0.5]));
					weights = weights.add(val.clip(0, 1));
				},
				\sixteenth, {
					rhythmFigures = rhythmFigures.add(Pseq([0.25, 0.25]));
					weights = weights.add(val.clip(0, 1));
				},
				\thirtyseconds, {
					rhythmFigures = rhythmFigures.add(Pseq([0.125, 0.125]));
					weights = weights.add(val.clip(0, 1));
				},
				\dotedEight, {
					rhythmFigures = rhythmFigures.add(Pseq([0.75, 0.25]));
					weights = weights.add(val.clip(0, 1));
				},
				\reverseDotedEight, {
					rhythmFigures = rhythmFigures.add(Pseq([0.25, 0.75]));
					weights = weights.add(val.clip(0, 1));
				},
				\tie, {
					// TODO
					/*					rhythmFigures = rhythmFigures.add(Pseq([1]));
					weights = weights.add(val);*/
				}
			);
		});

		if((rhythmFigures.size > 0 && weights.indexOfGreaterThan(0).notNil), {
			Pbindef(name, \dur, Pwrand(rhythmFigures, weights.normalizeSum, inf));
		}, {
			Pbindef(name, \dur, 1);
		});
	}

	changeLength {arg newLength;
		var quant = Pdef((name++"_repeater").asSymbol).quant;
		Pdef((name++"_repeater").asSymbol).quant = length;

		length = newLength;

		Pdef((name++"_repeater").asSymbol, Pn(Pfindur(newLength, Pseed(currentSeed, Pbindef(name))), inf));

		Pdef((name++"_repeater").asSymbol).quant = quant;
	}

	regenerate {
		currentSeed = 2147483647.rand;
		Pdef((name++"_repeater").asSymbol, Pn(Pfindur(length, Pseed(currentSeed, Pbindef(name))), inf));
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