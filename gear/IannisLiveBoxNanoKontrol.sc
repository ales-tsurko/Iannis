IannisLiveBoxNanoKontrol {
	var loggerTextFields,
	ccsState,
	controlsMap,
	<deviceID,
	<controlSynth,
	values,
	midiFunc,
	<mapping,
	modulationSwitchState,
	zValues,
	keys;

	*new {arg loggerTextFields, controlSynth;
		^super.new.init(loggerTextFields, controlSynth);
	}

	init {arg aloggerTextFields, acontrolSynth;
		controlSynth = acontrolSynth;
		loggerTextFields = aloggerTextFields;


		if (this.initDevice()) {
			this.onNanoKontrolInit();
		} {
			this.initLoggerTextFields();
			loggerTextFields.do({arg tf;
				tf.string = "nanoKontrol doesn't initialized";
			});
		};
	}

	initDevice {
		var midiIn;
		MIDIClient.init;
		MIDIIn.connectAll;
		midiIn = MIDIIn.findPort("nanoKONTROL", "SLIDER/KNOB");
		if (midiIn.isNil) {
			^false;
		} {
			deviceID = midiIn.uid;
			^true;
		};
	}

	onNanoKontrolInit {
		ccsState = 0!127;
		values = ();

		this.initLoggerTextFields();
		this.initKeys();
		this.initModulationSwitchState();
		this.initZValues();
		this.initControlsMap();
		this.initMapping();
		this.initMIDIFunc();
	}

	initLoggerTextFields {
		AppClock.sched(0.0, {
			loggerTextFields.do({arg tf, n;
				tf.enabled = false;
				tf.string = "Sampler" + (n+1);
			});
		});
	}

	initKeys {
		keys = [\cursor, \dur, \att, \rel, \oct, \gain];
		keys.do({arg key;
			keys = keys++[(key++2).asSymbol];
			keys = keys++[(\r++key).asSymbol];
			keys = keys++[(\r++key++2).asSymbol];
		});
	}

	initModulationSwitchState {
		modulationSwitchState = ();
		keys.do({arg key;
			modulationSwitchState[key] = false;
		})
	}

	initZValues {
		zValues = ();
		zValues.cursor = 0;
		zValues.att = 1;
		zValues.oct = 63;
		zValues.cursor2 = 0;
		zValues.att2 = 1;
		zValues.oct2 = 63;
		zValues.dur = 127;
		zValues.rel = 33;
		zValues.gain = 63;
		zValues.dur2 = 20;
		zValues.rel2 = 33;
		zValues.gain2 = 63;

		zValues.rcursor = 0;
		zValues.ratt = 0;
		zValues.rcursor2 = 0;
		zValues.ratt2 = 0;
		zValues.rdur = 0;
		zValues.rrel = 0;
		zValues.rdur2 = 0;
		zValues.rrel2 = 0;
	}

	initControlsMap {
		controlsMap = ();
		controlsMap.topButtonsCCs = (23..31);
		controlsMap.bottomButtonsCCs = [53,54,55,57,59,60,62,64,66];
		controlsMap.knobs = (14..22);
		controlsMap.faders = (2..7)++[9,12,13];
	}

	initMapping {
		mapping = ()!127;

		9.do({arg n;
			mapping[controlsMap.knobs[n]].rSwitchCC = controlsMap.topButtonsCCs[n];
			mapping[controlsMap.topButtonsCCs[n]].switchingParameter = controlsMap.knobs[n];

			mapping[controlsMap.faders[n]].rSwitchCC = controlsMap.bottomButtonsCCs[n];
			mapping[controlsMap.bottomButtonsCCs[n]].switchingParameter = controlsMap.faders[n];
		});

		// Knobs
		mapping[controlsMap.knobs[0]].key = \cursor;
		mapping[controlsMap.knobs[1]].key = \att;
		mapping[controlsMap.knobs[2]].key = \oct;
		mapping[controlsMap.knobs[3]].key = \cursor2;
		mapping[controlsMap.knobs[4]].key = \att2;
		mapping[controlsMap.knobs[5]].key = \oct2;

		// Faders
		mapping[controlsMap.faders[0]].key = \dur;
		mapping[controlsMap.faders[1]].key = \rel;
		mapping[controlsMap.faders[2]].key = \gain;
		mapping[controlsMap.faders[3]].key = \dur2;
		mapping[controlsMap.faders[4]].key = \rel2;
		mapping[controlsMap.faders[5]].key = \gain2;

		// Top Buttons
		mapping[controlsMap.topButtonsCCs[0]].key = \cursor;
		mapping[controlsMap.topButtonsCCs[1]].key = \att;
		mapping[controlsMap.topButtonsCCs[3]].key = \cursor2;
		mapping[controlsMap.topButtonsCCs[4]].key = \att2;

		// Bottom Buttons
		mapping[controlsMap.bottomButtonsCCs[0]].key = \dur;
		mapping[controlsMap.bottomButtonsCCs[1]].key = \rel;
		mapping[controlsMap.bottomButtonsCCs[3]].key = \dur2;
		mapping[controlsMap.bottomButtonsCCs[4]].key = \rel2;
	}

	initMIDIFunc {
		midiFunc = MIDIFunc.cc({arg val, num, ch, id;
			ccsState[num] = val;

			if (this.isCCButton(num)) {
				if (mapping[num].key.notNil) {
					this.onSwitchButtonForMapWithNum(mapping[num], num);
				}
			} {
				if (mapping[num].key.notNil) {
					case
					{ (mapping[num].key == \att) || (mapping[num].key == \att2) } {
						this.onChangeContinuousCCForMapWithNumAndValue(mapping[num], num, val, {arg value;
							value/127 * 7;
						});
					}
					{ (mapping[num].key == \rel) || (mapping[num].key == \rel2) } {
						this.onChangeContinuousCCForMapWithNumAndValue(mapping[num], num, val, {arg value;
							value/127 * 15;
						});
					}
					{ (mapping[num].key == \oct) || (mapping[num].key == \oct2) } {
						this.onChangeContinuousCCForMapWithNumAndValue(mapping[num], num, val, {arg value;
							0.25*(2**(value/27).floor);
						});
					}
					{ true } {
						this.onChangeContinuousCCForMapWithNumAndValue(mapping[num], num, val);
					};
				}
			};
		}, srcID: deviceID);
	}

	onSwitchButtonForMapWithNum {arg map, num;
		var key = map.key;
		var isRandomlyModulated = ccsState[num].asBoolean;
		var rKey = (\r ++ key).asSymbol;
		var loggerIndex = this.getLoggerIndexForKey(key);

		if (isRandomlyModulated) {
			if (values[rKey].notNil) {
				this.updateAndDisplaySynthValueForKey(rKey, loggerIndex, \percent);
				modulationSwitchState[rKey] = false;
			}
		} {
			if (values[key].notNil) {
				controlSynth.set(rKey, 0);
				modulationSwitchState[key] = false;
				this.displayValueAtKeyAsType(loggerIndex, key, \normal);
			}
		}
	}

	onChangeContinuousCCForMapWithNumAndValue {arg map, num, val, valueTransformer;
		var key = map.key;
		var switchCC = map.rSwitchCC;
		var isRandomlyModulated = mapping[switchCC].key.notNil && ccsState[switchCC].asBoolean;
		var value;
		var rKey = (\r ++ key).asSymbol;
		var loggerIndex = this.getLoggerIndexForKey(key);

		if (valueTransformer.notNil) {
			value = valueTransformer.value(val);
		} {
			value = val/127;
		};

		if (isRandomlyModulated) {
			if (modulationSwitchState[rKey]) {
				this.setAndDisplayValueForKey(rKey, value, loggerIndex, \percent);
				zValues[rKey] = val.round(2);
			};

			if (modulationSwitchState[rKey].not) {
				modulationSwitchState[rKey] = (zValues[key].round(2) == val.round(2));
			};
		} {
			if (modulationSwitchState[key]) {
				this.setAndDisplayValueForKey(key, value, loggerIndex, \normal);
				zValues[key] = val.round(2);
			};

			if (modulationSwitchState[key].not) {
				modulationSwitchState[key] = (zValues[key].round(2) == val.round(2));
			};
		}
	}

	getLoggerIndexForKey {arg key;
		// should be changed to more safe solution
		var loggerIndex;
		if (key.asString.last.asString != "2") { loggerIndex = 0 } { loggerIndex = 1 };
		^loggerIndex;
	}

	isCCButton {arg num;
		^(controlsMap.topButtonsCCs.includes(num) || controlsMap.bottomButtonsCCs.includes(num));
	}

	setAndDisplayValueForKey {arg key, value, loggerIndex, displayType;
		this.setValueForKey(key, value);
		this.displayValueAtKeyAsType(loggerIndex, key, displayType);
	}

	updateAndDisplaySynthValueForKey {arg key, loggerIndex, displayType;
		this.updateSynthValueForKey(key);
		this.displayValueAtKeyAsType(loggerIndex, key, displayType);
	}

	setValueForKey {arg key, value;
		values[key] = value;
		controlSynth.set(key, value);
	}

	updateSynthValueForKey {arg key;
		var value = values[key];
		controlSynth.set(key, value);
	}

	displayValueAtKeyAsType {arg index, key, type;
		switch(type,
			\percent, {this.displayPercentValueAtKey(index, key)},
			\normal, {this.displayNormalValueAtKey(index, key)}
		);
	}

	displayNormalValueAtKey {arg index, key;
		var value = values[key].round(0.01);
		var str = key.asString++":"+value;
		this.updateLoggerAtIndexWithString(index, str);
	}

	displayPercentValueAtKey {arg index, key;
		var value = (values[key] * 100).round(0.01);
		var str = key++":"+value++"%";
		this.updateLoggerAtIndexWithString(0, str);
	}

	updateLoggerAtIndexWithString {arg index, string;
		AppClock.sched(0.0, {
			loggerTextFields[index].string = string;
		});
	}

	free {
		midiFunc.free();
	}
}
