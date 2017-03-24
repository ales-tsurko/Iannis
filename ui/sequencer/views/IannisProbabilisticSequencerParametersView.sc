IannisProbabilisticSequencerParametersView : CompositeView {
	var playStopButton, resetButton, updateButton,
	patternLengthField, seedField, correspondingSequencer,
  rootNumberBox, scalePopup, tuningPopup,
  quantizationBox, offsetBox,
  swingKnob,
  arrayOfNotes, playTimesBox;

	*new {arg sequencer;
		^super.new.init(sequencer);
	}

	init {arg sequencer;
		var patternLengthLabel = StaticText.new;
		var seedLabel = StaticText.new;
    var rootLabel = StaticText.new;
    var rootPitchRepresentation = StaticText.new;
    var scaleLabel = StaticText.new;
    var tuningLabel = StaticText.new;
    var playTimesLabel = StaticText.new;
    var beatCounter = StaticText.new;
    var quantizationLabel = StaticText.new;
    var offsetLabel = StaticText.new;
    var swingLabel = StaticText.new;
    var swingValueLabel = StaticText.new;
    var pitches = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];
    correspondingSequencer = sequencer;

    // beat counter
    beatCounter.string = (correspondingSequencer.time+1).asString;
    beatCounter.font = Font("Arial", 42);
    correspondingSequencer.timeAction = {arg newTime;
      beatCounter.string = newTime.asString;
    };

    // root
    // init array of notes
    arrayOfNotes = [];
    128.do({arg i;
      var newNote = pitches[i%12]++(i/12).floor;
      arrayOfNotes = arrayOfNotes.add(newNote);
    });

    rootLabel.string = "Root:";
    rootNumberBox = NumberBox.new;
    rootNumberBox.fixedWidth = 50;
    rootNumberBox.decimals = 0;
    rootNumberBox.clipLo = 0;
    rootNumberBox.clipHi = 127;
    rootNumberBox.alt_scale = 3;
    rootNumberBox.shift_scale = 12;

    rootNumberBox.action = {arg box;
      rootLabel.string = "Root:" + arrayOfNotes[box.value];
      correspondingSequencer.root = box.value - 60;
    };

    rootNumberBox.valueAction = 60;

    // scale
    scaleLabel.string = "Scale:";
    scalePopup = PopUpMenu.new;
    scalePopup.items = [];

    Scale.names.do({arg name;
      var newScale = Scale.at(name).name;
      scalePopup.items = scalePopup.items.add(newScale);
    });

    scalePopup.action = {arg popup;
      var newScale = Scale.newFromKey(Scale.names[popup.value]);
      correspondingSequencer.scale = newScale;
    };

    scalePopup.valueAction = 11; // index of chromatic scale

    // tuning
    tuningLabel.string = "Tuning:";
    tuningPopup = PopUpMenu.new;
    tuningPopup.items = [];


    Tuning.names.do({arg name;
      var newTuning = Tuning.at(name).name;
      tuningPopup.items = tuningPopup.items.add(newTuning);
    });

    tuningPopup.action = {arg popup;
      var newTuning = Tuning.names[popup.value];
      correspondingSequencer.scale.tuning = newTuning;
    };
    
    tuningPopup.valueAction = 3; // index of ET12

		// pattern length
		patternLengthLabel.string = "Pattern length (beats):";
		patternLengthField = TextField.new;
		patternLengthField.fixedWidth = 125;
		patternLengthField.action = {arg field;
        this.lengthFieldAction(field);
		};

		patternLengthField.valueAction = 8;

    // play times
    playTimesLabel.string = "Play times:";
    playTimesBox = NumberBox.new;
    playTimesBox.fixedWidth = 50;
    playTimesBox.decimals = 0;
    playTimesBox.clipLo = 0;
    playTimesBox.clipHi = 999;

    playTimesBox.action = {arg box;
      correspondingSequencer.playTimes = box.value;
    };

		// seed
		seedLabel.string = "Version number:";
		seedField = TextField.new;
		seedField.fixedWidth = 100;

		seedField.action = {arg field;
        this.seedFieldAction(field);
		};
    		
    seedField.valueAction = correspondingSequencer.seed;

    // quantization
    quantizationLabel.string = "Quantization:";
    quantizationBox = NumberBox.new;
    quantizationBox.minDecimals = 0;
    quantizationBox.maxDecimals = 4;
    quantizationBox.alt_scale = 0.25;
    quantizationBox.shift_scale = 1;
    quantizationBox.fixedWidth = 50;

    quantizationBox.action = {arg box;
      sequencer.setQuantization(box.value);
    };

    quantizationBox.valueAction = 1;

    // offset
    offsetLabel.string = "Offset:";
    offsetBox = NumberBox.new;
    offsetBox.fixedWidth = 50;
    offsetBox.minDecimals = 0;
    offsetBox.maxDecimals = 4;
    offsetBox.alt_scale = 0.25;
    offsetBox.shift_scale = 1;
    offsetBox.clipLo = 0;

    offsetBox.action = {arg box;
      sequencer.setQuantization(quantizationBox.value, box.value);
    };

    offsetBox.valueAction = 0;

    // swing
    swingLabel.string = "Swing:";
    swingValueLabel.align = \center;
    swingKnob = Knob.new;
    swingKnob.mode = \vert;

    swingKnob.action = {arg knob;
      sequencer.shuffle = knob.value;
      swingValueLabel.string = (knob.value.round(0.01)*100).asString++"%";
    };

    swingKnob.valueAction = 0;

		// play/stop button
		playStopButton = Button.new;
		playStopButton.states = [["Play"], ["Stop"]];
		playStopButton.fixedWidth = 200;

		playStopButton.action = {arg button;
			this.playStopButtonAction(button);
		};

    correspondingSequencer.addOnFinishAction({playStopButton.value = 0});

		// reset button
		resetButton = Button.new;
		resetButton.states = [["Reset"]];
		resetButton.fixedWidth = 200;

		resetButton.action = {arg button;
			this.resetButtonAction(button);
		};

		// update button
		updateButton = Button.new;
		updateButton.states = [["Random Version"]];
		updateButton.fixedWidth = 200;

		updateButton.action = {arg button;
			this.updateButtonAction(button);
		};

    this.layout = HLayout(
      VLayout(
        HLayout(
          playStopButton, resetButton, updateButton,
        ),
        HLayout(
          patternLengthLabel, patternLengthField, playTimesLabel, playTimesBox,
          nil,
          seedLabel, seedField
        ),
        HLayout(
          // quantization
          quantizationLabel, quantizationBox,
          offsetLabel, offsetBox,
          nil,
          //swing
          VLayout(VLayout(nil,swingLabel,nil),nil), VLayout(swingKnob, swingValueLabel)
        )
      ),

      nil,
      beatCounter,
      nil,

      VLayout(
        nil,
        HLayout(
          nil, 
          rootLabel, rootNumberBox, rootPitchRepresentation, 
          scaleLabel, scalePopup
        ),
        HLayout(
          nil, tuningLabel, tuningPopup 
        ),
        nil
      )
    );

		this.fixedHeight = 130;
	}

  lengthFieldAction {arg field;
    var expr = field.value.interpret;
    // if there is no anything except spaces -- assign 4
    if(field.value.findRegexp("[^ \t]").size == 0, {
      field.value = 4;
      expr = 4;
    });

    if(expr.notNil, {
      correspondingSequencer.length = expr;
    });
  }

  seedFieldAction {arg field;
    var expr = field.value.interpret;
    // if there is no anything except spaces -- assign 4
    if(field.value.findRegexp("[^ \t]").size == 0, {
      field.value = correspondingSequencer.seed;
      expr = correspondingSequencer.seed;
    });

    if(expr.notNil, {
      correspondingSequencer.seed = expr;
    });
  }


	playStopButtonAction {arg button;
		if(button.value == 1, {
			correspondingSequencer.play();
		}, {
			correspondingSequencer.stop();
		});
	}

	resetButtonAction {arg button;
		correspondingSequencer.reset();
	}

	updateButtonAction {arg button;
		correspondingSequencer.regenerate();

		// update seed field
		seedField.value = correspondingSequencer.seed;
	}

}
