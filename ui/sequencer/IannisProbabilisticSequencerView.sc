IannisProbabilisticSequencerView : CompositeView {
	var <rhythmView, <parametersView, <stepsView;

	*new {arg name, numberOfSteps;
		^super.new.init(name, numberOfSteps);
	}

	init {arg name, numberOfSteps;
		rhythmView = IannisProbabilisticSequencerRhythmView.new;
		stepsView = IannisProbabilisticSequencerMultipleStepsView.new(numberOfSteps);
		parametersView = IannisProbabilisticSequencerParametersView.new(numberOfSteps);

		//
		// Layout
		//
		this.layout = VLayout(rhythmView, stepsView, parametersView);
	}

}