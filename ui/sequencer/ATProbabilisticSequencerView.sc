ATProbabilisticSequencerView : CompositeView {
	var parametersPanel, numberOfStepsField, transpositionField,
	updateButton, randomizeStepsProbsButton,
	stepsProbabilitiesScrollView, stepsProbabilitiesCanvasView;
	// должны быть параметры:
	// количество шагов
	// транспозиция
	// randomize probabilities
	// update

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
		var stepsNumberLabel = StaticText.new;
		stepsNumberLabel.string = "Number of steps";
		// parameters panel initialization
		parametersPanel = CompositeView.new;
		parametersPanel.fixedHeight = 50;

		// number of steps field
		numberOfStepsField = TextField.new;
		numberOfStepsField.fixedWidth = 50;

		numberOfStepsField.action = {arg field;
			var stepsNum = field.value.asInt.clip(2, 128);
			numberOfStepsField.value = stepsNum;
		};

		numberOfStepsField.valueAction = numberOfSteps;

		// update button
		updateButton = Button.new;
		updateButton.states = [["Generate Pattern"]];
		updateButton.fixedWidth = 220;

		// randomize steps button
		randomizeStepsProbsButton = Button.new;
		randomizeStepsProbsButton.states = [["Randomize Probabilities"]];
		randomizeStepsProbsButton.fixedWidth = 250;

		// Parameters panel layout
		parametersPanel.layout = HLayout(stepsNumberLabel, numberOfStepsField, nil, updateButton, randomizeStepsProbsButton);


		//
		// Steps
		//
		this.updateSteps(numberOfSteps);

		//
		// Layout
		//
		this.layout = VLayout(parametersPanel, stepsProbabilitiesScrollView);
	}

	updateSteps {arg numberOfSteps;
		stepsProbabilitiesScrollView = ScrollView.new;
		stepsProbabilitiesCanvasView = CompositeView();

		// initialize sliders
		stepsProbabilitiesCanvasView.layout = GridLayout.rows(
			Array.fill(numberOfSteps, {arg n;
				var ch = ATProbabilisticSequencerStepView.new(n+1);
				ch.fixedWidth = 150;
				ch;
			});
		);

		stepsProbabilitiesCanvasView.minHeight = 135;

		stepsProbabilitiesScrollView.hasHorizontalScroller = true;
		stepsProbabilitiesScrollView.hasVerticalScroller = false;
		stepsProbabilitiesScrollView.hasBorder = false;

		stepsProbabilitiesScrollView.canvas = stepsProbabilitiesCanvasView;
	}

}