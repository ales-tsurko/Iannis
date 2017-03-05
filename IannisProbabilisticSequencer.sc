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

		this.regenerate();
	}

	updateRhythm {arg duration;
		var rhythmPattern;
		Pbindef(name, \dur, duration);
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