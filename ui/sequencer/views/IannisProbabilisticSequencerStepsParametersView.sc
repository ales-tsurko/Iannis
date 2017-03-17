IannisProbabilisticSequencerStepsParametersView : CompositeView {
	var <numberOfStepsBox, <setAllProbsBox, <setAllExprsField,
	<transpositionField, <mulField, <randomizeProbsButton,
	<parentController;

	*new {arg numberOfSteps, parentController;
		^super.new.init(numberOfSteps, parentController);
	}

	init {arg numberOfSteps, parentCtrlr;
		var setAllExprsLabel = StaticText.new;
		var setAllProbsLabel = StaticText.new;
		var stepsNumberLabel = StaticText.new;
		var transpositionLabel = StaticText.new;
    var mulLabel = StaticText.new;

		parentController = parentCtrlr;

		// number of steps field
		stepsNumberLabel.string = "Number of steps:";
    numberOfStepsBox = NumberBox.new;
    numberOfStepsBox.decimals = 0;
    numberOfStepsBox.clipLo = 1;
    numberOfStepsBox.clipHi = 128;
    numberOfStepsBox.alt_scale = 2;
    numberOfStepsBox.shift_scale = 4;
    numberOfStepsBox.fixedWidth = 50;

    numberOfStepsBox.addAction({arg box;
      parentController.stepsView.updateParentData();
      parentController.stepsView.removeAll;
      parentController.stepsView.updateSteps(box.value.asInt);
    }, \mouseUpAction);

    numberOfStepsBox.keyUpAction = {arg box, char, mod, unicode, keycode, key;
      if (keycode == 36) {
        numberOfStepsBox.mouseUpAction.(numberOfStepsBox);
      };
    };

		// multiplication field
		mulLabel.string = "Multiply:";
		mulField = TextField.new;
		mulField.fixedWidth = 125;
		mulField.action = {arg field;
			var expr = field.value.interpret;
			// if there is no anything except spaces -- assign 1
			if(field.value.findRegexp("[^ \t]").size == 0, {
				field.value = 1;
				expr = 1;
			});

			if(expr.notNil, {
				parentController.data[\mul] = expr;
				parentController.updatePattern();
			});
		};

		mulField.valueAction = 1;

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
				parentController.data[\transposition] = expr;
				parentController.updatePattern();
			});
		};

		transpositionField.valueAction = 0;

		// set all expressions field
		setAllExprsLabel.string = "Set all expressions to:";
		setAllExprsField = TextField.new;
		setAllExprsField.fixedWidth = 125;

		setAllExprsField.action = {arg field;
				// update all the expression fields with a new value
				parentController.stepsView.canvas.children.do({arg item;
					if(item.isKindOf(IannisProbabilisticSequencerStepView), {
						item.expressionField.valueAction = field.value;
					});
				});
		};

		// set all probabilities field
    setAllProbsLabel.string = "Set all probabilities to:";
    setAllProbsBox = NumberBox.new;
    setAllProbsBox.fixedWidth = 50;
    setAllProbsBox.decimals = 3;
    setAllProbsBox.value = 0.0;
    setAllProbsBox.clipLo = 0.0;
    setAllProbsBox.clipHi = 1.0;
    setAllProbsBox.step = 0.05;
    setAllProbsBox.scroll_step = 0.05;
    setAllProbsBox.alt_scale = 0.05;
    setAllProbsBox.shift_scale = 2;

    setAllProbsBox.addAction({arg box;
      // update all the probabilies slider with a new value
      parentController.stepsView.canvas.children.do({arg item;
        if(item.isKindOf(IannisProbabilisticSequencerStepView), {
          item.probabilitySlider.valueAction = box.value;
        });
      });
    }, \mouseUpAction);

    setAllProbsBox.keyUpAction = {arg box, char, mod, unicode, keycode, key;
      if (keycode == 36) {
        setAllProbsBox.mouseUpAction.(setAllProbsBox);
      };
    };

		// randomize probs button
		randomizeProbsButton = Button.new;
		randomizeProbsButton.states = [["Randomize Probabilities"]];
		randomizeProbsButton.fixedWidth = 200;

		randomizeProbsButton.action = {arg button;
			parentController.stepsView.canvas.children.do({arg item;
				if(item.isKindOf(IannisProbabilisticSequencerStepView), {
					if(item.probabilitySlider.enabled, {
						item.probabilitySlider.value = 1.0.rand;
            item.probabilitySlider.mouseUpAction();
					});
				});
			});
		};


		this.layout = VLayout(
			nil,

      HLayout(
        nil,
        // steps number
        stepsNumberLabel, numberOfStepsBox,
      ),

			HLayout(
				nil,
				// multiply
				mulLabel, mulField,
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
				setAllProbsLabel, setAllProbsBox,
			),
			// buttons
			HLayout(randomizeProbsButton)
		);

		this.fixedWidth = 300;
	}
}
