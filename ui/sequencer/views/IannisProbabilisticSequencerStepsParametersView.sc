IannisProbabilisticSequencerStepsParametersView : CompositeView {
	var <numberOfStepsField, <setAllProbsField, <setAllExprsField,
	<transpositionField, <randomizeProbsButton,
	<correspondingStepsView;

	*new {arg numberOfSteps, stepsView;
		^super.new.init(numberOfSteps, stepsView);
	}

	init {arg numberOfSteps, stepsView;
		var setAllExprsLabel = StaticText.new;
		var setAllProbsLabel = StaticText.new;
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

		// numberOfStepsField.focusLostAction = {arg view;
			// numberOfStepsField.doAction;
		// };


		// transposition field
		transpositionLabel.string = "Add (transpose):";
		transpositionField = TextField.new;
		transpositionField.fixedWidth = 125;
		transpositionField.action = {arg field;
			var expr = field.value.interpret;
			// if there is no except spaces -- assign 0
			if(field.value.findRegexp("[^ \t]").size == 0, {
				field.value = 0;
				expr = 0;
			});

			if(expr.notNil, {
				correspondingStepsView.transposition = expr;
				correspondingStepsView.stepAction();
			});
		};

		// transpositionField.focusLostAction = {arg view;
			// transpositionField.doAction;
		// };

		transpositionField.valueAction = 0;

		// set all expressions field
		setAllExprsLabel.string = "Set all expressions to:";
		setAllExprsField = TextField.new;
		setAllExprsField.fixedWidth = 125;

		setAllExprsField.action = {arg field;
				// update all the expression fields with a new value
				correspondingStepsView.canvas.children.do({arg item;
					if(item.isKindOf(IannisProbabilisticSequencerStepView), {
						item.expressionField.valueAction = field.value;
					});
				});
		};

		// setAllExprsField.focusLostAction = {arg view;
			// setAllExprsField.doAction;
		// };

		// set all probabilities field
		setAllProbsLabel.string = "Set all probabilities to:";
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

		// setAllProbsField.focusLostAction = {arg view;
			// setAllProbsField.doAction;
		// };

		// randomize steps button
		randomizeProbsButton = Button.new;
		randomizeProbsButton.states = [["Randomize Probabilities"]];
		randomizeProbsButton.fixedWidth = 200;

		randomizeProbsButton.action = {arg button;
			correspondingStepsView.canvas.children.do({arg item;
				if(item.isKindOf(IannisProbabilisticSequencerStepView), {
					if(item.probabilitySlider.enabled, {
						item.probabilitySlider.valueAction = 1.0.rand;
					});
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
				// set all expressions
				setAllExprsLabel, setAllExprsField
			),

			HLayout(
				nil,
				// set all probs
				setAllProbsLabel, setAllProbsField,
			),
			// buttons
			HLayout(randomizeProbsButton)
		);

		this.fixedWidth = 300;
	}
}
