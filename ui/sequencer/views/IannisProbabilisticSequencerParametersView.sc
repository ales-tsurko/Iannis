IannisProbabilisticSequencerParametersView : CompositeView {
	var playStopButton, resetButton, updateButton,
	patternLengthField;

	*new {
		^super.new.init();
	}

	init {
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

		this.layout = VLayout(
			// buttons
			HLayout(
				playStopButton,
				resetButton,
				updateButton,
				nil
			),

			HLayout(
				patternLengthLabel, patternLengthField,
				nil
			)
		);

		this.fixedHeight = 75;
	}
}