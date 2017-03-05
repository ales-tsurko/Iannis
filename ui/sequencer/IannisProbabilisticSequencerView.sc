IannisProbabilisticSequencerView : CompositeView {
	var <rhythmView, <parametersView, <stepsView;

	// нужно ограничить максимальное количество  шагов. например, 128
	// (количество миди-нот — то есть при 128 шагах можно определить вероятность
	// каждой из возможных нот))

	// получается, что вью нужно разбить на три части:
	// панель с параметрами (количество шагов, транспозиция... свинг?)
	// ритм
	// тоны (scrollview)
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