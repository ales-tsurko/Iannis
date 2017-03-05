IannisProbabilisticSequencerRhythmViewKnob : CompositeView {
	var <probabilityKnob, image, imageView, <probabilityTextField,
	<rhythmSymbol;

	*new {arg imagePath, rhythmSymbol;
		^super.new.init(imagePath, rhythmSymbol);
	}

	init {arg imagePath, rhythmSymbol;
		rhythmSymbol = rhythmSymbol;
		// image
		imageView = CompositeView.new;
		image = Image.open(imagePath);
		imageView.fixedWidth = image.width;
		imageView.fixedHeight = image.height;
		imageView.backgroundImage_(image);

		// knob
		probabilityKnob = Knob.new;
		probabilityKnob.mode = \vert;

		// text field
		probabilityTextField = TextField.new;
		probabilityTextField.string = probabilityKnob.value.asString;

		probabilityTextField.action = {arg tField;
			probabilityKnob.valueAction = tField.value.asFloat;
		};

		// knob ation
		probabilityKnob.action = {arg knob;
			probabilityTextField.string = knob.value.round(0.001).asString;
		};

		this.onClose = {image.free};

		this.fixedWidth = 55;

		this.layout = VLayout(VLayout(nil, HLayout(imageView)), probabilityKnob, probabilityTextField);
	}
}