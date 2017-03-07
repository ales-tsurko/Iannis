IannisProbabilisticSequencerStepsParametersView : CompositeView {
	var <numberOfStepsField, <setAllProbsField, <transpositionField,
	<randomizeProbsButton, <correspondingStepsView;

	*new {arg numberOfSteps, stepsView;
		^super.new.init(numberOfSteps, stepsView);
	}

	init {arg numberOfSteps, stepsView;
		var setAllPitchesProbsLabel = StaticText.new;
		var stepsNumberLabel = StaticText.new;
		var transpositionLabel = StaticText.new;

		correspondingStepsView = stepsView;

		// number of steps field
		stepsNumberLabel.string = "Number of steps:";
		numberOfStepsField = TextField.new;
		numberOfStepsField.fixedWidth = 50;

		numberOfStepsField.value = numberOfSteps;

		numberOfStepsField.action = {arg field;
			var stepsNum = field.value.asInt.clip(2, 128);
			numberOfStepsField.value = stepsNum;

			correspondingStepsView.updateData();
			correspondingStepsView.removeAll;
			correspondingStepsView.updateSteps(stepsNum);
		};


		// transposition field
		transpositionLabel.string = "Transpose:";
		transpositionField = TextField.new;
		transpositionField.fixedWidth = 50;
		transpositionField.action = {arg field;
			var newValue = field.value.asFloat.clip(-128, 128);
			transpositionField.value = newValue;
		};

		transpositionField.valueAction = 0;

		// set all ptiches probabilities field
		setAllPitchesProbsLabel.string = "Set all probabilities to:";
		setAllProbsField = TextField.new;
		setAllProbsField.fixedWidth = 50;

		setAllProbsField.value = 0;

		setAllProbsField.action = {arg field;
			var newValue = field.value.asFloat.clip(0, 1);
			setAllProbsField.value = newValue;

			// update all the probabilies slider with a new value
			correspondingStepsView.canvas.children.do({arg item;
				if(item.isKindOf(IannisProbabilisticSequencerStepView), {
					item.probabilitySlider.valueAction = newValue;
				});
			});
		};

		// randomize steps button
		randomizeProbsButton = Button.new;
		randomizeProbsButton.states = [["Randomize Probabilities"]];
		randomizeProbsButton.fixedWidth = 200;

		randomizeProbsButton.action = {arg button;
			correspondingStepsView.canvas.children.do({arg item;
				if(item.isKindOf(IannisProbabilisticSequencerStepView), {
					item.probabilitySlider.valueAction = 1.0.rand;
				});
			});
		};


		this.layout = VLayout(
			nil,

			HLayout(
				nil,
				// steps number
				stepsNumberLabel, numberOfStepsField,
			),

			HLayout(
				nil,
				// transpose
				transpositionLabel, transpositionField,
			),

			HLayout(
				nil,
				// set all probs
				setAllPitchesProbsLabel, setAllProbsField,
			),
			// buttons
			HLayout(randomizeProbsButton)
		);

		this.fixedWidth = 250;
		this.background = Color.gray(0.925, 1);
	}
}