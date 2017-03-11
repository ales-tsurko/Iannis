IannisProbabilisticSequencerView : CompositeView {
	var <rhythmStepsView, <rhythmParametersView,
	<pitchStepsView, <pitchParametersView,
  <pitchController, <rhythmController,
	<parametersView,
	<sequencer;

	*new {arg name, instrument, numberOfPitches = 4, numberOfRhythmicFigures = 2, patternLength = 8;
		^super.new.init(name, instrument, numberOfPitches, numberOfRhythmicFigures, patternLength);
	}

	init {arg name, instrument, numberOfPitches, numberOfRhythmicFigures, patternLength;
		sequencer = IannisProbabilisticSequencer(name, instrument, patternLength);

    pitchController = IannisProbabilisticSequencerEventController(sequencer, "Pitch", \midinote, numberOfPitches);
    pitchController.stepsView.canvas.background = Color.gray(0.79, 1);
		pitchController.parametersView.background = Color.gray(0.925, 1);
		// pitchStepsView = IannisProbabilisticSequencerMultipleStepsView.new(sequencer, numberOfPitches, "Pitch", \midinote);
		// pitchParametersView = IannisProbabilisticSequencerStepsParametersView.new(numberOfPitches, pitchStepsView);
		// pitchStepsView.canvas.background = Color.gray(0.79, 1);
		// pitchParametersView.background = Color.gray(0.925, 1);

    rhythmController = IannisProbabilisticSequencerEventController(sequencer, "Rhythm", \dur, numberOfRhythmicFigures);
    rhythmController.stepsView.canvas.background = Color.gray(0.72, 1);
    rhythmController.parametersView.background = Color.gray(0.85, 1);
		// rhythmStepsView = IannisProbabilisticSequencerMultipleStepsView.new(sequencer, numberOfRhythmicFigures, "Rhythm", \dur);
		// rhythmParametersView = IannisProbabilisticSequencerStepsParametersView.new(numberOfRhythmicFigures, rhythmStepsView);
		// rhythmStepsView.canvas.background = Color.gray(0.72, 1);
		// rhythmParametersView.background = Color.gray(0.85, 1);

		parametersView = IannisProbabilisticSequencerParametersView.new(sequencer);


		//
		// Layout
		//
		this.layout = VLayout(
      pitchController,
      rhythmController,
			// HLayout(pitchStepsView, pitchParametersView),
			// HLayout(rhythmStepsView, rhythmParametersView),
			parametersView
		);

		this.layout.spacing = 0;
	}

}
