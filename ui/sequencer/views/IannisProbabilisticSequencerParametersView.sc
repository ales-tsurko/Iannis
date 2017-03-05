IannisProbabilisticSequencerParametersView : CompositeView {
	var numberOfStepsField, setAllPitchesProbabilitiesField,
	setAllRhythmProbabilitiesField, transpositionField,
	playStopButton, resetButton, updateButton,
	patternLengthField,
	randomizeStepsProbsButton, randomizeRhythmProbsButton;

	*new {arg numberOfSteps;
		^super.new.init(numberOfSteps);
	}

	init {arg numberOfSteps;
		var setAllPitchesProbsLabel = StaticText.new;
		var setAllRhythmProbsLabel = StaticText.new;
		var stepsNumberLabel = StaticText.new;
		var transpositionLabel = StaticText.new;
		var patternLengthLabel = StaticText.new;

		// pattern length
		patternLengthLabel.string = "Pattern length (beats):";
		patternLengthField = TextField.new;
		patternLengthField.fixedWidth = 50;

		patternLengthField.value = 8;

		patternLengthField.action = {arg field;
			var length = field.value.asInt.clip(1, 999);
			patternLengthField.value = length;
		};

		// number of steps field
		stepsNumberLabel.string = "Number of pitches:";
		numberOfStepsField = TextField.new;
		numberOfStepsField.fixedWidth = 50;

		numberOfStepsField.value = numberOfSteps;

		numberOfStepsField.action = {arg field;
			var stepsNum = field.value.asInt.clip(2, 128);
			numberOfStepsField.value = stepsNum;

			this.parent.stepsView.updateData();
			this.parent.stepsView.removeAll;
			this.parent.stepsView.updateSteps(stepsNum);
		};


		// transposition field
		transpositionLabel.string = "Transpose:";
		transpositionField = TextField.new;
		transpositionField.fixedWidth = 50;
		transpositionField.action = {arg field;
			var newValue = field.value.asFloat.clip(-60, 60);
			transpositionField.value = newValue;
		};

		transpositionField.valueAction = 0;

		// set all ptiches probabilities field
		setAllPitchesProbsLabel.string = "Set all pitches probabilities to:";
		setAllPitchesProbabilitiesField = TextField.new;
		setAllPitchesProbabilitiesField.fixedWidth = 50;

		setAllPitchesProbabilitiesField.value = 0;

		setAllPitchesProbabilitiesField.action = {arg field;
			var newValue = field.value.asFloat.clip(0, 1);
			setAllPitchesProbabilitiesField.value = newValue;

			// update all the probabilies slider with a new value
			this.parent.stepsView.canvas.children.do({arg item;
				if(item.isKindOf(IannisProbabilisticSequencerStepView), {
					item.probabilitySlider.valueAction = newValue;
				});
			});
		};

		// set all rhythm probabilities field
		setAllRhythmProbsLabel.string = "Set all rhythm probabilities to:";
		setAllRhythmProbabilitiesField = TextField.new;
		setAllRhythmProbabilitiesField.fixedWidth = 50;

		setAllRhythmProbabilitiesField.value = 0;

		setAllRhythmProbabilitiesField.action = {arg field;
			var newValue = field.value.asFloat.clip(0, 1);
			setAllRhythmProbabilitiesField.value = newValue;

			// update all the probabilies knobs with a new value
			this.parent.rhythmView.knobs.do({arg item;
				item.probabilityKnob.valueAction = newValue;
			});
		};

		// play/stop button
		playStopButton = Button.new;
		playStopButton.states = [["Play"], ["Stop"]];
		playStopButton.fixedWidth = 200;

		// reset button
		resetButton = Button.new;
		resetButton.states = [["Reset"]];
		resetButton.fixedWidth = 200;

		// update button
		updateButton = Button.new;
		updateButton.states = [["Regenerate"]];
		updateButton.fixedWidth = 200;

		// randomize steps button
		randomizeStepsProbsButton = Button.new;
		randomizeStepsProbsButton.states = [["Randomize Pitches Probs"]];
		randomizeStepsProbsButton.fixedWidth = 200;

		randomizeStepsProbsButton.action = {arg button;
			this.parent.stepsView.canvas.children.do({arg item;
				if(item.isKindOf(IannisProbabilisticSequencerStepView), {
					item.probabilitySlider.valueAction = 1.0.rand;
				});
			});
		};

		// randomize rhythm button
		randomizeRhythmProbsButton = Button.new;
		randomizeRhythmProbsButton.states = [["Randomize Rhythm Probs"]];
		randomizeRhythmProbsButton.fixedWidth = 200;

		randomizeRhythmProbsButton.action = {arg button;
			this.parent.rhythmView.knobs.do({arg item;
				item.probabilityKnob.valueAction = 1.0.rand;
			});
		};


		this.layout = VLayout(
			// buttons
			HLayout(
				playStopButton,
				resetButton,
				updateButton,
				nil,
				randomizeStepsProbsButton,
				randomizeRhythmProbsButton
			),

			HLayout(
				nil,
				// steps number
				stepsNumberLabel, numberOfStepsField,
				// transpose
				transpositionLabel, transpositionField,

			),

			HLayout(
				patternLengthLabel,
				patternLengthField,
				nil,
				// set all probs
				setAllPitchesProbsLabel, setAllPitchesProbabilitiesField,
				setAllRhythmProbsLabel,
				setAllRhythmProbabilitiesField
			)
		);

		this.fixedHeight = 150;
	}
}