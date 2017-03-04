ATProbabilisticSequencerView : CompositeView {
	var parametersPanel, numberOfStepsField, setAllProbabilitiesField, transpositionField,
	playStopButton, resetButton, updateButton, randomizeStepsProbsButton,
	stepsData,
	stepsProbabilitiesScrollView, stepsProbabilitiesCanvasView;
	// должны быть параметры:
	// транспозиция

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
		var setAllProbsLabel = StaticText.new;
		var stepsNumberLabel = StaticText.new;
		var transpositionLabel = StaticText.new;

		// parameters panel initialization
		parametersPanel = CompositeView.new;
		parametersPanel.fixedHeight = 100;

		// number of steps field
		stepsNumberLabel.string = "Number of steps:";
		numberOfStepsField = TextField.new;
		numberOfStepsField.fixedWidth = 50;

		numberOfStepsField.valueAction = numberOfSteps;

		numberOfStepsField.action = {arg field;
			var stepsNum = field.value.asInt.clip(2, 128);
			numberOfStepsField.value = stepsNum;

			this.updateStepsData();
			stepsProbabilitiesScrollView.removeAll;
			this.updateSteps(stepsNum);
		};


		// transposition field
		transpositionLabel.string = "Transpose:";
		transpositionField = TextField.new;
		transpositionField.fixedWidth = 50;
		transpositionField.action = {arg field;
			var newValue = field.value.asFloat.clip(-60, 60);
			transpositionField.value = newValue;
		};

		transpositionField.valueAction = 0;

		// set all probabilities field
		setAllProbsLabel.string = "Set all probabilities to:";
		setAllProbabilitiesField = TextField.new;
		setAllProbabilitiesField.fixedWidth = 50;

		setAllProbabilitiesField.action = {arg field;
			var newValue = field.value.asFloat.clip(0, 1);
			setAllProbabilitiesField.value = newValue;

			// update all the probabilies slider with a new value
			stepsProbabilitiesCanvasView.children.do({arg item;
				if(item.isKindOf(ATProbabilisticSequencerStepView), {
					item.probabilitySlider.valueAction = newValue;
				});
			});
		};

		// play/stop button
		playStopButton = Button.new;
		playStopButton.states = [["Play"], ["Stop"]];
		playStopButton.fixedWidth = 200;

		// reset button
		resetButton = Button.new;
		resetButton.states = [["Reset"]];
		resetButton.fixedWidth = 200;

		// update button
		updateButton = Button.new;
		updateButton.states = [["Regenerate Pattern"]];
		updateButton.fixedWidth = 200;

		// randomize steps button
		randomizeStepsProbsButton = Button.new;
		randomizeStepsProbsButton.states = [["Randomize Probabilities"]];
		randomizeStepsProbsButton.fixedWidth = 200;

		randomizeStepsProbsButton.action = {arg button;
			stepsProbabilitiesCanvasView.children.do({arg item;
				if(item.isKindOf(ATProbabilisticSequencerStepView), {
					item.probabilitySlider.valueAction = 1.0.rand;
				});
			});
		};

		// Parameters panel layout
		parametersPanel.layout = VLayout(
			// buttons
			HLayout(
				playStopButton,
				resetButton,
				nil,
				updateButton,
				randomizeStepsProbsButton),

			HLayout(
				nil,
				// steps number
				stepsNumberLabel, numberOfStepsField,
				// transpose
				transpositionLabel, transpositionField,
				// set all probs
				setAllProbsLabel, setAllProbabilitiesField)
		);


		//
		// Steps
		//
		stepsProbabilitiesScrollView = ScrollView.new;
		stepsProbabilitiesCanvasView = CompositeView();
		stepsProbabilitiesCanvasView.minHeight = 135;

		stepsProbabilitiesScrollView.hasHorizontalScroller = true;
		stepsProbabilitiesScrollView.hasVerticalScroller = false;
		stepsProbabilitiesScrollView.hasBorder = false;
		stepsProbabilitiesScrollView.canvas = stepsProbabilitiesCanvasView;

		stepsData = Array.fill(128, {
			var dict = Dictionary.new;
			dict[\pitchIndex] = 60;
			dict[\probability] = 0;
		});

		this.updateSteps(numberOfSteps);

		//
		// Layout
		//
		this.layout = VLayout(stepsProbabilitiesScrollView, parametersPanel);
	}

	updateSteps {arg numberOfSteps;
		// initialize sliders
		stepsProbabilitiesCanvasView.layout = GridLayout.rows(
			Array.fill(numberOfSteps, {arg n;
				var ch = ATProbabilisticSequencerStepView.new(n+1);
				ch.fixedWidth = 80;

				if(stepsData[n].notNil, {
					ch.probabilitySlider.valueAction = stepsData[n][\probability];
					ch.pitchPopUp.valueAction = stepsData[n][\pitchIndex];
				});

				// return
				ch;
			});
		);
	}

	updateStepsData {
		stepsProbabilitiesCanvasView.children.do({arg item;
			if(item.isKindOf(ATProbabilisticSequencerStepView), {
				var n = item.label.string.asInt - 1;
				stepsData[n][\probability] = item.probabilitySlider.value;
				stepsData[n][\pitchIndex] = item.pitchPopUp.value;
			});
		});
	}

}