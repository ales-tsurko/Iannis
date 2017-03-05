IannisProbabilisticSequencerParametersView : CompositeView {
		var numberOfStepsField, setAllProbabilitiesField, transpositionField,
	playStopButton, resetButton, updateButton, randomizeStepsProbsButton;

	*new {arg numberOfSteps, stepsView;
		^super.new.init(numberOfSteps, stepsView);
	}

	init {arg numberOfSteps, stepsView;
		var setAllProbsLabel = StaticText.new;
		var stepsNumberLabel = StaticText.new;
		var transpositionLabel = StaticText.new;

		// parameters panel initialization
		this.fixedHeight = 100;

		// number of steps field
		stepsNumberLabel.string = "Number of steps:";
		numberOfStepsField = TextField.new;
		numberOfStepsField.fixedWidth = 50;

		numberOfStepsField.valueAction = numberOfSteps;

		numberOfStepsField.action = {arg field;
			var stepsNum = field.value.asInt.clip(2, 128);
			numberOfStepsField.value = stepsNum;

			stepsView.updateData();
			stepsView.removeAll;
			stepsView.updateSteps(stepsNum);
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
			stepsView.canvas.children.do({arg item;
				if(item.isKindOf(IannisProbabilisticSequencerStepView), {
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
			stepsView.canvas.children.do({arg item;
				if(item.isKindOf(IannisProbabilisticSequencerStepView), {
					item.probabilitySlider.valueAction = 1.0.rand;
				});
			});
		};

		this.layout = VLayout(
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
	}
}