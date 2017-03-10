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

		expressionField.focusLostAction = {arg view;
			expressionField.doAction;
		};

		// text field
		probabilityTextField = TextField.new;
		probabilityTextField.string = probabilitySlider.value.asString;
		probabilityTextField.fixedWidth = 65;

		probabilityTextField.action = {arg tField;
			probabilitySlider.valueAction = tField.value.asFloat;

			delegate.stepAction(this);
		};

		probabilityTextField.focusLostAction = {arg view;
			probabilityTextField.doAction;
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

		this.fixedWidth = 225;
		this.layout = VLayout(
			nil,
			HLayout(sliderLabel, expressionField),
			HLayout(nil, probabilitySlider, probabilityTextField),
			nil
		);
	}
}
