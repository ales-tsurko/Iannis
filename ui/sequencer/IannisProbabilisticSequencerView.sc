IannisProbabilisticSequencerView : CompositeView {
	var <rhythmStepsView, <rhythmParametersView, <pitchStepsView, <pitchParametersView, <parametersView;

	*new {arg name, numberOfSteps;
		^super.new.init(name, numberOfSteps);
	}

	init {arg name, numberOfSteps;
		rhythmStepsView = IannisProbabilisticSequencerMultipleStepsView.new(numberOfSteps, "Rhythm");
		rhythmParametersView = IannisProbabilisticSequencerStepsParametersView.new(numberOfSteps, rhythmStepsView);

		pitchStepsView = IannisProbabilisticSequencerMultipleStepsView.new(numberOfSteps, "Pitch");
		pitchParametersView = IannisProbabilisticSequencerStepsParametersView.new(numberOfSteps, pitchStepsView);

		parametersView = IannisProbabilisticSequencerParametersView.new;


		//
		// Layout
		//
		this.layout = VLayout(
			HLayout(rhythmStepsView, rhythmParametersView),
			HLayout(pitchStepsView, pitchParametersView),
			parametersView
		);

		this.layout.spacing = 0;
	}

}