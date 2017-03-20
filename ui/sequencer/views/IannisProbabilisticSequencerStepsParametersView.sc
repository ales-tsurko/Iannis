IannisProbabilisticSequencerStepsParametersView : CompositeView {
	var <numberOfStepsBox, <setAllProbsBox, <setAllExprsField,
	<transpositionField, <transpositionDurBox, 
  <mulField, <mulDurBox,
  <randomizeProbsButton,
	<parentController;

	*new {arg numberOfSteps, parentController;
		^super.new.init(numberOfSteps, parentController);
	}

	init {arg numberOfSteps, parentCtrlr;
		var setAllExprsLabel = StaticText.new;
		var setAllProbsLabel = StaticText.new;
		var stepsNumberLabel = StaticText.new;
		var transpositionLabel = StaticText.new;
    var transposeDurLabel = StaticText.new;
    var mulLabel = StaticText.new;
    var mulDurLabel = StaticText.new;

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

    numberOfStepsBox.value = numberOfSteps;

		// multiplication field
    // duration is needed in action, so initialize it here
    mulDurLabel.string = "Mul duration:";
    mulDurBox = NumberBox.new;
    mulDurBox.value = 1;

		mulLabel.string = "Multiply:";
		mulField = TextField.new;
		mulField.fixedWidth = 125;
		mulField.action = {arg field;
      parentController.setMul(field, mulDurBox.value);
      parentController.updatePattern();
		};

		mulField.valueAction = 1;

    // multiplication duration
    mulDurBox.minDecimals = 0;
    mulDurBox.maxDecimals = 4;
    mulDurBox.clipLo = 0.015;
    mulDurBox.clipHi = 32;
    mulDurBox.alt_scale = 0.25;
    mulDurBox.shift_scale = 1;
    mulDurBox.fixedWidth = 50;

    mulDurBox.action = {arg box;
      parentController.setMul(mulField, box.value);
    };

		// transposition field
    transposeDurLabel.string = "Add duration:";
    transpositionDurBox = NumberBox.new;
    transpositionDurBox.value = 1;

		transpositionLabel.string = "Add (transpose):";
		transpositionField = TextField.new;
		transpositionField.fixedWidth = 125;
		transpositionField.action = {arg field;
      parentController.setAdd(field, transpositionDurBox.value);
      parentController.updatePattern();
		};

		transpositionField.valueAction = 0;

    // transposition duration
    transpositionDurBox.minDecimals = 0;
    transpositionDurBox.maxDecimals = 4;
    transpositionDurBox.clipLo = 0.015;
    transpositionDurBox.clipHi = 32;
    transpositionDurBox.alt_scale = 0.25;
    transpositionDurBox.shift_scale = 1;
    transpositionDurBox.fixedWidth = 50;

    transpositionDurBox.action = {arg box;
      parentController.setAdd(transpositionField, box.value);
    };

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
        stepsNumberLabel, numberOfStepsBox
      ),

			HLayout(
				nil,
				// multiply
				mulLabel, mulField
			),
      // mul dur
      HLayout(
        nil,
        mulDurLabel, mulDurBox
      ),

			HLayout(
				nil,
				// transpose
				transpositionLabel, transpositionField
			),
      // transpose dur
      HLayout(
        nil,
        transposeDurLabel, transpositionDurBox
      ),

			HLayout(
				nil,
				// set all expressions
				setAllExprsLabel, setAllExprsField
			),

			HLayout(
				nil,
				// set all probs
				setAllProbsLabel, setAllProbsBox
			),
			// buttons
			HLayout(randomizeProbsButton)
		);

		this.fixedWidth = 300;
	}
}
