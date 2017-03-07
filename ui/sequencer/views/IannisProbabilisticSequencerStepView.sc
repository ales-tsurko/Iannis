IannisProbabilisticSequencerStepView : CompositeView {
	var <probabilityTextField, <probabilitySlider,
	<expressionField, <sliderLabel,
	<number, <realExpression;

	*new {arg name, number, delegate;
		^super.new.init(name, number, delegate);
	}

	init {arg name, orderNumber, delegate;
		number = orderNumber;

		probabilitySlider = Knob().mode_(\vert).fixedWidth_(33);

		// expression field
		expressionField = TextField.new;

		expressionField.action = {arg field;
			realExpression = field.value.interpret;

			// block probability on error
			if(realExpression.isNil, {
				probabilitySlider.valueAction = 0;

				probabilitySlider.enabled = false;
				probabilityTextField.enabled = false;
			}, {
				probabilitySlider.enabled = true;
				probabilityTextField.enabled = true;
			});

			delegate.stepAction(this);
		};

		// text field
		probabilityTextField = TextField.new;
		probabilityTextField.string = probabilitySlider.value.asString;
		probabilityTextField.fixedWidth = 65;

		probabilityTextField.action = {arg tField;
			probabilitySlider.valueAction = tField.value.asFloat;

			delegate.stepAction(this);
		};

		// slider ation
		probabilitySlider.action = {arg slider;
			probabilityTextField.string = slider.value.round(0.001).asString;
		};

		probabilitySlider.mouseUpAction = {arg slider;
			delegate.stepAction(this);
		};

		// sliderLabel
		sliderLabel = StaticText.new;
		sliderLabel.string = name.asString++":";
		sliderLabel.align = \center;

		// init default value for expression field
		expressionField.valueAction = 60;

		this.fixedWidth = 225;
		this.layout = VLayout(
			nil,
			HLayout(sliderLabel, expressionField),
			HLayout(nil, probabilitySlider, probabilityTextField),
			nil
		);
	}
}