IannisProbabilisticSequencerStepView : CompositeView {
	var <probabilityTextField, <probabilitySlider, <pitchPopUp, <label;

	*new {arg name;
		^super.new.init(name);
	}

	init {arg name;
		var pitches = ["G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#"];

		probabilitySlider = Slider().orientation_(\vertical).fixedWidth_(21);
		pitchPopUp = PopUpMenu.new;
		pitchPopUp.items = [];

		128.do({arg i;
			var newPitch = pitches[i%11]++(i/12).floor;
			pitchPopUp.items = pitchPopUp.items.add(newPitch);
		});

		// text field
		probabilityTextField = TextField.new;
		probabilityTextField.string = probabilitySlider.value.asString;

		probabilityTextField.action = {arg tField;
			probabilitySlider.valueAction = tField.value.asFloat;
		};

		// slider ation
		probabilitySlider.action = {arg slider;
			probabilityTextField.string = slider.value.round(0.001).asString;
		};

		// label
		label = StaticText.new;
		label.string = name;
		label.align = \center;

		this.layout = VLayout(label, pitchPopUp, HLayout(probabilitySlider), probabilityTextField);
	}
}