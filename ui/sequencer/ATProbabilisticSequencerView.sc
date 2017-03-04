ATProbabilisticSequencerView : CompositeView {
	var stepsProbabilitiesScrollView, stepsProbabilitiesCanvasView;
	// должны быть параметры:
	// количество шагов
	// транспозиция
	// randomize probabilities

	// само вью должно быть scroll, потому что шагов пользователь может сделать
	// вообще дохрена (и нужно ограничить максимальное количество... например, 128
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
		this.updateSteps(numberOfSteps);
	}

	updateSteps {arg numberOfSteps;
		stepsProbabilitiesScrollView = ScrollView.new;
		stepsProbabilitiesCanvasView = CompositeView();

		// initialize sliders
		stepsProbabilitiesCanvasView.layout = GridLayout.rows(
			Array.fill(numberOfSteps, {arg n;
				var ch = ATProbabilisticSequencerStepView.new(n+1);
				ch.bounds.width = 150;
				ch;
			});
		);

		stepsProbabilitiesCanvasView.minHeight = 135;

		stepsProbabilitiesScrollView.hasHorizontalScroller = true;
		stepsProbabilitiesScrollView.hasVerticalScroller = false;
		stepsProbabilitiesScrollView.hasBorder = false;

		stepsProbabilitiesScrollView.canvas = stepsProbabilitiesCanvasView;

		this.layout = VLayout(stepsProbabilitiesScrollView);
	}
}