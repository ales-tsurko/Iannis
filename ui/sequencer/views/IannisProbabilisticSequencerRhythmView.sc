IannisProbabilisticSequencerRhythmView : CompositeView {
	var <knobs;

	*new {
		^super.new.init();
	}

	init {
		knobs = [];
		knobs = knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/quarter.png".resolveRelative, \quarter));
		knobs =	knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/doted_quarter.png".resolveRelative, \dotedQuarter));
		knobs =	knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/half.png".resolveRelative, \half));
		knobs =	knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/doted_half.png".resolveRelative, \dotedHalf));
		knobs = knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/whole.png".resolveRelative, \whole));
		knobs = knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/eight.png".resolveRelative, \eight));
		knobs =	knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/sixteenth.png".resolveRelative, \sixteenth));
		knobs =	knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/32nds.png".resolveRelative, \thirtyseconds));
		knobs =	knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/doted_eight.png".resolveRelative, \dotedEight));
		knobs =	knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/reverse_doted_eight.png".resolveRelative, \reverseDotedEight));
		knobs =	knobs.add(IannisProbabilisticSequencerRhythmViewKnob.new("img/tie.png".resolveRelative, \tie));

		this.fixedHeight = 150;

		this.layout = HLayout(*knobs);
	}
}