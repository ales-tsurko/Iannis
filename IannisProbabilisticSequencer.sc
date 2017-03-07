IannisProbabilisticSequencer {
	var <name, <synthName, <length, <currentSeed;

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

	updateEvent {arg key, values, weights, transposition;
		var pattern = Pwrand(values.asList, weights.normalizeSum, inf);
		Pbindef(name, key, pattern+transposition);
	}

	changeLength {arg newLength;
		var quant = Pdef((name++"_repeater").asSymbol).quant;
		Pdef((name++"_repeater").asSymbol).quant = length;

		length = newLength;

		Pdef((name++"_repeater").asSymbol, Pn(Pfindur(newLength, Pseed(currentSeed, Pbindef(name))), inf));

		Pdef((name++"_repeater").asSymbol).quant = quant;
	}

	setSeed {arg seed;
		currentSeed = seed;
		Pdef((name++"_repeater").asSymbol, Pn(Pfindur(length, Pseed(currentSeed, Pbindef(name))), inf));
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