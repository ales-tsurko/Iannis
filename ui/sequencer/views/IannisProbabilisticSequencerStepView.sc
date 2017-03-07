IannisProbabilisticSequencerStepView : CompositeView {
	var <probabilityTextField, <probabilitySlider, <expressionField, <sliderLabel;

	*new {arg name;
		^super.new.init(name);
	}

	init {arg name;
		probabilitySlider = Knob().mode_(\vert).fixedWidth_(33);

		// expression field
		expressionField = TextField.new;
		expressionField.string = "60";

		// a = ("["++expressionField.value++"]").interpret


		// text field
		probabilityTextField = TextField.new;
		probabilityTextField.string = probabilitySlider.value.asString;
		probabilityTextField.fixedWidth = 65;

		probabilityTextField.action = {arg tField;
			probabilitySlider.valueAction = tField.value.asFloat;
		};

		// slider ation
		probabilitySlider.action = {arg slider;
			probabilityTextField.string = slider.value.round(0.001).asString;
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