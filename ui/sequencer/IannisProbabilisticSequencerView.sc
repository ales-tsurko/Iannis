IannisProbabilisticSequencerView : CompositeView {
	var <rhythmStepsView, <rhythmParametersView, <pitchStepsView, <pitchParametersView, <parametersView;

	*new {arg name, numberOfPitches = 7, numberOfRhythmicFigures = 2;
		^super.new.init(name, numberOfPitches, numberOfRhythmicFigures);
	}

	init {arg name, numberOfPitches, numberOfRhythmicFigures;
		pitchStepsView = IannisProbabilisticSequencerMultipleStepsView.new(numberOfPitches, "Pitch");
		pitchStepsView.canvas.background = Color.gray(0.79, 1);
		pitchParametersView = IannisProbabilisticSequencerStepsParametersView.new(numberOfPitches, pitchStepsView);
		pitchParametersView.background = Color.gray(0.925, 1);

		rhythmStepsView = IannisProbabilisticSequencerMultipleStepsView.new(numberOfRhythmicFigures, "Rhythm");
		rhythmStepsView.canvas.background = Color.gray(0.72, 1);
		rhythmParametersView = IannisProbabilisticSequencerStepsParametersView.new(numberOfRhythmicFigures, rhythmStepsView);
		rhythmParametersView.background = Color.gray(0.85, 1);
		rhythmParametersView.setAllExprsField.valueAction = 1;

		parametersView = IannisProbabilisticSequencerParametersView.new;


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