IannisProbabilisticSequencerView : CompositeView {
	var <rhythmStepsView, <rhythmParametersView,
	<pitchStepsView, <pitchParametersView,
	<parametersView,
	<sequencer;

	*new {arg name, instrument, numberOfPitches = 7, numberOfRhythmicFigures = 2, patternLength = 8;
		^super.new.init(name, instrument, numberOfPitches, numberOfRhythmicFigures, patternLength);
	}

	init {arg name, instrument, numberOfPitches, numberOfRhythmicFigures, patternLength;
		sequencer = IannisProbabilisticSequencer(name, instrument, patternLength);

		pitchStepsView = IannisProbabilisticSequencerMultipleStepsView.new(sequencer, numberOfPitches, "Pitch", \midinote);
		pitchParametersView = IannisProbabilisticSequencerStepsParametersView.new(numberOfPitches, pitchStepsView);
		pitchStepsView.canvas.background = Color.gray(0.79, 1);
		pitchParametersView.background = Color.gray(0.925, 1);

		rhythmStepsView = IannisProbabilisticSequencerMultipleStepsView.new(sequencer, numberOfRhythmicFigures, "Rhythm", \dur);
		rhythmParametersView = IannisProbabilisticSequencerStepsParametersView.new(numberOfRhythmicFigures, rhythmStepsView);
		rhythmStepsView.canvas.background = Color.gray(0.72, 1);
		rhythmParametersView.background = Color.gray(0.85, 1);
		rhythmParametersView.setAllExprsField.valueAction = 1;

		parametersView = IannisProbabilisticSequencerParametersView.new(sequencer);


		//
		// Layout
		//
		this.layout = VLayout(
			HLayout(pitchStepsView, pitchParametersView),
			HLayout(rhythmStepsView, rhythmParametersView),
			parametersView
		);

		this.layout.spacing = 0;
	}

}