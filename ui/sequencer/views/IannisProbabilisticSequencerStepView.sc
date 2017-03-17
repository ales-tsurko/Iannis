IannisProbabilisticSequencerStepView : CompositeView {
	var <probabilitySlider,
	<expressionField, <sliderLabel,
	<number, <realExpression;

	*new {arg name, number, parentView;
		^super.new.init(name, number, parentView);
	}

	init {arg name, orderNumber, parentView;
    var weightLabel = StaticText.new;
		number = orderNumber;

		// expression field
		expressionField = TextField.new;

		expressionField.action = {arg field;
			realExpression = field.value.interpret;

			// block probability on error
			if(realExpression.isNil, {
				probabilitySlider.valueAction = 0;

				probabilitySlider.enabled = false;
			}, {
				probabilitySlider.enabled = true;
			});

			parentView.parentController.stepAction(this);
		};

		expressionField.focusLostAction = {arg view;
			expressionField.doAction;
		};

		// slider
    weightLabel.string = "Weight:";
    probabilitySlider = NumberBox.new;
    probabilitySlider.fixedWidth = 50;
    probabilitySlider.decimals = 3;
    probabilitySlider.clipLo = 0.0;
    probabilitySlider.clipHi = 1.0;
    probabilitySlider.step = 0.05;
    probabilitySlider.scroll_step = 0.05;
    probabilitySlider.alt_scale = 0.05;
    probabilitySlider.shift_scale = 2;

    probabilitySlider.addAction({
      parentView.parentController.stepAction(this);
    }, \mouseUpAction);

    probabilitySlider.keyUpAction = {arg box, char, mod, unicode, keycode, key;
      if (keycode == 36) {
        probabilitySlider.mouseUpAction.(probabilitySlider);
      };
    };

		// sliderLabel
		sliderLabel = StaticText.new;
		sliderLabel.string = name.asString++":";
		sliderLabel.align = \center;

		this.fixedWidth = 225;
		this.layout = VLayout(
			nil,
			HLayout(sliderLabel, expressionField),
			HLayout(nil, weightLabel, probabilitySlider),
			nil
		);
	}
}
