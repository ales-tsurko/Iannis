IannisLiveBoxLaunchpadS {
    var loggerTextFields,
    controlSynth,
    deviceID,
    state,
    midiOut,
    animations,
    currentMode,
    midiFuncs,
    controlsMap,
    isCurrentLayoutDrumRack = true,
    currentPageIndex = 0,
    isModeStepSequencer = false,
    isModeStepSequencerEdit = false,
    pitchersBusses,
    recordingDirectory;

	*new {arg loggerTextFields, controlSynth, pitchersBusses, recordingDirectory;
		^super.new.init(loggerTextFields, controlSynth, pitchersBusses, recordingDirectory);
	}

	init {arg aloggerTextFields, acontrolSynth, apitchersBusses, arecordingDirectory;
		controlSynth = acontrolSynth;
		loggerTextFields = aloggerTextFields;
        pitchersBusses = apitchersBusses;
        recordingDirectory = arecordingDirectory;

		if (this.initDevice()) {
			this.onDeviceInitSuccess();
		} {
			this.initLoggerTextFields();
			loggerTextFields.do({arg tf;
				tf.string = "Launchpad doesn't initialized";
			});
		};
	}

	initDevice {
		var midiIn;
		MIDIClient.init;
		MIDIIn.connectAll;
		midiIn = MIDIIn.findPort("Launchpad S", "Launchpad S");
		if (midiIn.isNil) {
			^false;
		} {
			deviceID = midiIn.uid;
            this.initMIDIOut();
			^true;
		};
	}

    initMIDIOut {
        midiOut = MIDIOut.newByName("Launchpad S", "Launchpad S");
    }

    initState {
        state = ()!2;
        2.do({arg n;
            state[n].isArm = false;
            state[n].samplePlaybackDirection = 1; // 1 - forward, -1 - backward
            state[n].nnToggle = 0!128;
            state[n].isOctRandomised = [false,false]; // up,down
            state[n].selectedPadToRecordInto = 0;
            state[n].isRecording = false;
            state[n].pitches = 1!128;
        });
    }

    initControlsMap {
        controlsMap = ();
        controlsMap.topButtonsCCs = (104..111);
        controlsMap.rightButtonsNNs = ();
        controlsMap.rightButtonsNNs.drumRackMode = (100..107);
        controlsMap.rightButtonsNNs.xyMode = [];
        8.do({arg n;
            var nn = (n*2+1)*8;
            controlsMap.rightButtonsNNs.xyMode = controlsMap.rightButtonsNNs.xyMode.add(nn);
        });

        controlsMap.mainGridNNs.xyMode = [];
        8.do({arg row;
            var startNote = row*16;
            var notes = (startNote..(startNote+7));
            controlsMap.mainGridNNs.xyMode = controlsMap.mainGridNNs.xyMode++notes;
        });

        {
            var firstHalf, secondHalf;
            firstHalf = (36..67).reshape(8,4).reverse;
            secondHalf = (68..99).reshape(8,4).reverse;
            controlsMap.mainGridNNs.drumRackMode = ([firstHalf].add(secondHalf)).lace(16).flat();
        }
    }

    getNNForGridButtonAtIndex {arg index;
        if (isCurrentLayoutDrumRack) {
            ^controlsMap.mainGridNNs.drumRackMode[index];
        } {
            ^controlsMap.mainGridNNs.xyMode[index];
        };
    }

    getNNForRightFunctionButtonAtIndex {arg index;
        if (isCurrentLayoutDrumRack) {
            ^controlsMap.rightButtonsNNs.drumRackMode[index];
        } {
            ^controlsMap.rightButtonsNNs.xyMode[index];
        };
    }

    initAnimations {
        animations = ()!2;

        this.initArmAnimations();
    }

    initArmAnimations {
        animations.arm = Task({
            loop {
                // midiOut.noteOn(0, 107, 15);
                midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 15);
                0.85.wait;
                // midiOut.noteOn(0, 107, 16);
                midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 16);
                0.15.wait;
            };
        });

        // animations[1].arm = Task({
            // loop {
                // midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 15);
                // 0.85.wait;
                // midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 16);
                // 0.15.wait;
            // };
        // });
    }

    initMIDIFuncs {
        midiFuncs = [];
        midiFuncs = midiFuncs++[this.initArmMIDIFunc()];
        midiFuncs = midiFuncs++[this.initTopButtonsMIDIFunc()];
    }

    initArmMIDIFunc {
        ^MIDIFunc.noteOn({arg vel, note, ch, id;
            if (note == controlsMap.rightButtonsNNs.drumRackMode[7]) {
                this.toggleArmForSynthIndex(currentPageIndex);
            };
        }, srcID: deviceID);
    }

    initTopButtonsMIDIFunc {
        ^MIDIFunc.cc({arg val, num, ch, id;
            if(num == controlsMap.topButtonsCCs[5]).or(num == controlsMap.topButtonsCCs[6]) {
                var pageIndex = (num == controlsMap.topButtonsCCs[6]).asInteger;
                this.switchPageTo(pageIndex);
            };

            if ((num == controlsMap.topButtonsCCs[0]).and(val == 127)) {
                this.toggleOctaveRandomizationForDirection(0);
            };

            if ((num == controlsMap.topButtonsCCs[1]).and(val == 127)) {
                this.toggleOctaveRandomizationForDirection(1);
            };

            if (((num == controlsMap.topButtonsCCs[2]).or(num == controlsMap.topButtonsCCs[3])).and(val == 127)) {
                var playbackDirection = if (num == controlsMap.topButtonsCCs[2]) {-1} {1};
                this.setSamplePlaybackDirection(playbackDirection);
            };

        }, srcID: deviceID);
    }

    switchPageTo {arg index;
        currentPageIndex = index;

        if (state[currentPageIndex].isArm) {
            animations.arm.play();
        } {
            animations.arm.stop();
        };

        midiOut.control(0, 0, 2);
        isCurrentLayoutDrumRack = true;
        isModeStepSequencer = false;
        isModeStepSequencerEdit = false;

        7.do({arg n;
            var buttonNN = this.getNNForRightFunctionButtonAtIndex(n);

            if ((currentPageIndex == 1).and(n < 2)) {
                midiOut.noteOn(0, buttonNN, 30);
            } {
                midiOut.noteOn(0, buttonNN, 29);
            };
        });

        ~seqNoteDurs.do({arg i, n;
            if (i == ~seqNoteDurCurrent) {midiOut.noteOn(0, 102 + n, 63)};
        });
        midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 16);

        4.do({arg n;
            4.do({arg i; 
                var index = 8*n+i;
                midiOut.noteOn(0, this.getNNForGridButtonAtIndex(index), 17);
                midiOut.noteOn(0, this.getNNForGridButtonAtIndex(index+4), 16);
                midiOut.noteOn(0, this.getNNForGridButtonAtIndex(index+32), 16);
                midiOut.noteOn(0, this.getNNForGridButtonAtIndex(index+36), 1);
            });
        });

        state[currentPageIndex].nnToggle.do({arg item, i;
            if(item == 1) {midiOut.noteOn(0, i, 48)};
        });

        2.do({arg n;
            state[1].isOctRandomised[n] = state[1].isOctRandomised[n].not;
            this.toggleOctaveRandomizationForDirection(n);
        });

        this.setSamplePlaybackDirection(state.samplePlaybackDirection[currentPageIndex]);
    }

    toggleArmForSynthIndex {arg index;
        if (state[index].isArm) {
            midiOut.noteOn(0, this.getNNForRightFunctionButtonAtIndex(7), 16);
            animations.arm.stop();
        } {
            animations.arm.play();
        };

        state[index].isArm = state[index].isArm.not;
    }

    setSamplePlaybackDirection {arg direction;
        var key = if (currentPageIndex == 0) {\direction1} {\direction2};
        var color1 = 63,
        color2 = 19;
        state.samplePlaybackDirection = direction;
        if(state.samplePlaybackDirection[currentPageIndex] == 1) {
            color1 = 19;
            color2 = 63;
        };
        midiOut.control(0, controlsMap.topButtonsCCs[2], color1);
        midiOut.control(0, controlsMap.topButtonsCCs[3], color2);

        controlSynth.set(key, state.samplePlaybackDirection[currentPageIndex]);

        this.logString("Play direction is:"+direction);
    }

    logString {arg value;
        loggerTextFields[currentPageIndex].string = value;
    }

    toggleOctaveRandomizationForDirection {arg direction; // direction = 0 (up), 1 (down)
        if (state[1].isOctRandomised[direction]) {
            midiOut.control(0, controlsMap.topButtonsCCs[direction], 19);
            controlSynth.set(\roct2, 1);
        } {
            midiOut.control(0, controlsMap.topButtonsCCs[direction], 63);
        };
        {
            //log
            var directionString = if {direction == 0} {"up"} {"down"};
            this.logString("ROct."+directionString+state[1].isOctRandomised[direction]);
        };

        state[1].isOctRandomised[direction] = state[1].isOctRandomised[direction].not;
    }

    setRecordingDirectory {arg newValue;
        recordingDirectory = newValue;
    }

	onDeviceInitSuccess {
        this.initControlsMap();
        this.initState();
        this.initMIDIFuncs();
        ~seqNoteDurs = [1/4, 1/8, 1/16, 1/32, 1/64] * 4;
        ~seqNoteDurCurrent = ~seqNoteDurs[2];



// ---------------------------------------------------------------------------- SYNTH 1

MIDIFunc.noteOn({arg vel, note, ch, id;
    if((currentPageIndex == 0).and(state[0].isArm), {
        var fileName, buffer, recorder, pitcher;
        if ((state[0].isRecording.not).and(controlsMap.mainGridNNs.drumRackMode.includes(note)).and((state[0].nnToggle.count({arg n; n == 1}) < 4).or(state[0].nnToggle[note] == 1))) {
            fileName = recordingDirectory +/+ ("Recording-" ++ thisThread.seconds.asString).replace(".", "-") ++ ".wav";
            buffer = Buffer.alloc(Server.default, 65536, 1);
            buffer.write(fileName, "wav", "int16", 0, 0, true);
            recorder = Synth.tail(nil, "recorder", ["bufnum", buffer]);
            pitcher = Synth("pitchtector");
            midiOut.noteOn(0, note, 15);
            state[0].isRecording = true;
            state[0].selectedPadToRecordInto = note;
        };

        if (state[0].isRecording) {
            fork {
                var nn = state[0].selectedPadToRecordInto;
                ~samples1[nn] = Buffer.read(Server.default, fileName);
                state[0].nnToggle[nn] = 1;
                pitchersBusses[0].get({arg v; state[0].pitches[nn] = v});

                Server.default.wait;

                midiOut.noteOn(0, nn, 48);

                state[0].nnToggle.do({arg item, i;
                    var sw = 0;
                    if((item == 1).and(~buttsCatLPs1.includes(i + 1) == false), {
                        ~buttsCatLPs1.do({arg cont, n;
                            switch(sw,
                                0, {if(cont == 0, {~buttsCatLPs1[n] = i + 1; sw = 1})},
                                1, {~buttsCatLPs1[n].postln}
                            );
                        });
                        sw = 0;
                    });
                });
                ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};

                state[0].isRecording = false;
                recorder.free;
                pitcher.free;
                buffer.close;
                buffer.free;
                fileName.free;
            };
        };

        if(state[0].nnToggle.count({arg n; n == 1}) < 4, {

            if((note >= 36).and(note <= 51), {

                switch(state[0].nnToggle[note],
                    0, {midiOut.noteOn(0, note, 48); state[0].nnToggle[note] = 1;

                        state[0].nnToggle.do({arg item, i;
                            var sw = 0;
                            if((item == 1).and(~buttsCatLPs1.includes(i + 1) == false), {
                                ~buttsCatLPs1.do({arg cont, n;
                                    switch(sw,
                                        0, {if(cont == 0, {~buttsCatLPs1[n] = i + 1; sw = 1})},
                                        1, {~buttsCatLPs1[n].postln})});
                                sw = 0});
                        });
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                    },
                    1, {midiOut.noteOn(0, note, 16); state[0].nnToggle[note] = 0;
                        ~buttsCatLPs1.do({arg item, i; if(state[0].nnToggle[item-1] == 0, {~buttsCatLPs1[i] = 0})});
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                });
            });

            if((note >= 52).and(note <= 67), {
                switch(state[0].nnToggle[note],
                    0, {midiOut.noteOn(0, note, 48); state[0].nnToggle[note] = 1;
                        state[0].nnToggle.do({arg item, i;
                            var sw = 0;
                            if((item == 1).and(~buttsCatLPs1.includes(i + 1) == false), {
                                ~buttsCatLPs1.do({arg cont, n;
                                    switch(sw,
                                        0, {if(cont == 0, {~buttsCatLPs1[n] = i + 1; sw = 1})},
                                        1, {~buttsCatLPs1[n].postln});
                                });
                                sw = 0});
                        });
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                    },
                    1, {midiOut.noteOn(0, note, 17); state[0].nnToggle[note] = 0;
                        ~buttsCatLPs1.do({arg item, i; if(state[0].nnToggle[item-1] == 0, {~buttsCatLPs1[i] = 0})});
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                });
            });

            if((note >= 68).and(note <= 83), {
                switch(state[0].nnToggle[note],
                    0, {midiOut.noteOn(0, note, 48); state[0].nnToggle[note] = 1;
                        state[0].nnToggle.do({arg item, i;
                            var sw = 0;
                            if((item == 1).and(~buttsCatLPs1.includes(i + 1) == false), {
                                ~buttsCatLPs1.do({arg cont, n;
                                    switch(sw,
                                        0, {if(cont == 0, {~buttsCatLPs1[n] = i + 1; sw = 1})},
                                        1, {~buttsCatLPs1[n].postln})});
                                sw = 0});
                        });
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                    },
                    1, {midiOut.noteOn(0, note, 1); state[0].nnToggle[note] = 0;
                        ~buttsCatLPs1.do({arg item, i; if(state[0].nnToggle[item-1] == 0, {~buttsCatLPs1[i] = 0})});
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                });
            });

            if((note >= 84).and(note <= 99), {
                switch(state[0].nnToggle[note],
                    0, {midiOut.noteOn(0, note, 48); state[0].nnToggle[note] = 1;
                        state[0].nnToggle.do({arg item, i;
                            var sw = 0;
                            if((item == 1).and(~buttsCatLPs1.includes(i + 1) == false), {
                                ~buttsCatLPs1.do({arg cont, n;
                                    switch(sw,
                                        0, {if(cont == 0, {~buttsCatLPs1[n] = i + 1; sw = 1})},
                                        1, {~buttsCatLPs1[n].postln})});
                                sw = 0});
                        });
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                    },
                    1, {midiOut.noteOn(0, note, 16); state[0].nnToggle[note] = 0;
                        ~buttsCatLPs1.do({arg item, i; if(state[0].nnToggle[item-1] == 0, {~buttsCatLPs1[i] = 0})});
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                });
            });
            },

            {
                if((note >= 36).and(note <= 51).and(state[0].nnToggle[note] == 1),
                    {midiOut.noteOn(0, note, 16); state[0].nnToggle[note] = 0;
                        ~buttsCatLPs1.do({arg item, i; if(state[0].nnToggle[item-1] == 0, {~buttsCatLPs1[i] = 0})});
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                });

                if((note >= 52).and(note <= 67).and(state[0].nnToggle[note] == 1),
                    {midiOut.noteOn(0, note, 17); state[0].nnToggle[note] = 0;
                        ~buttsCatLPs1.do({arg item, i; if(state[0].nnToggle[item-1] == 0, {~buttsCatLPs1[i] = 0})});
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                });

                if((note >= 68).and(note <= 83).and(state[0].nnToggle[note] == 1),
                    {midiOut.noteOn(0, note, 1); state[0].nnToggle[note] = 0;
                        ~buttsCatLPs1.do({arg item, i; if(state[0].nnToggle[item-1] == 0, {~buttsCatLPs1[i] = 0})});
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                });

                if((note >= 84).and(note <= 99).and(state[0].nnToggle[note] == 1),
                    {midiOut.noteOn(0, note, 16); state[0].nnToggle[note] = 0;
                        ~buttsCatLPs1.do({arg item, i; if(state[0].nnToggle[item-1] == 0, {~buttsCatLPs1[i] = 0})});
                        ~sampleNumber1 = ~buttsCatLPs1.as(Array);
                        ~sampleNumber1.do{| item, i | if( item == 0) {~sampleNumber1.do{| a | if(a != 0) {~sampleNumber1[i] = a}}}};
                });
        });
    });

    controlSynth.set(\b1, ~samples1[~sampleNumber1[0]].bufnum);
    controlSynth.set(\b2, ~samples1[~sampleNumber1[1]].bufnum);
    controlSynth.set(\b3, ~samples1[~sampleNumber1[2]].bufnum);
    controlSynth.set(\b4, ~samples1[~sampleNumber1[3]].bufnum);

    controlSynth.set(\p1, state[0].pitches[~sampleNumber1[0]]);
    controlSynth.set(\p2, state[0].pitches[~sampleNumber1[1]]);
    controlSynth.set(\p3, state[0].pitches[~sampleNumber1[2]]);
    controlSynth.set(\p4, state[0].pitches[~sampleNumber1[3]]);

    if(~unpitched.includes(~sampleNumber1[0]), {controlSynth.set(\st1, 0)}, {controlSynth.set(\st1, 1)});
    if(~unpitched.includes(~sampleNumber1[1]), {controlSynth.set(\st2, 0)}, {controlSynth.set(\st2, 1)});
    if(~unpitched.includes(~sampleNumber1[2]), {controlSynth.set(\st3, 0)}, {controlSynth.set(\st3, 1)});
    if(~unpitched.includes(~sampleNumber1[3]), {controlSynth.set(\st4, 0)}, {controlSynth.set(\st4, 1)});

    // ~buttsCatLPs1.postln;
    // ~sampleNumber1.postln;
        }, srcID: deviceID);
// 
// ~lprecbutnum2 = Array.fill(128, 0);
// 
// // ---------------------------------------------------------------------------- SYNTH 2
// 
// MIDIFunc.noteOn({arg vel, note, ch, id;
// 
	// if((currentPageIndex == 1).and(state[1].isArm.not).and(isModeStepSequencer.not).and(isModeStepSequencerEdit.not), {
		// ~lprecbutnum2.do({arg item, i;
			// if(item == 1, {
				// Routine.run{
					// ~samples2[i + 1] = Buffer.read(Server.default, ~recFileName2);
					// state[1].nnToggle[i] = 1;
					// pitchersBusses[1].get({arg v; state[1].pitches[i + 1] = v});
					// s.wait;
					// midiOut.noteOn(0, i + 36, 48);
// 
					// state[1].nnToggle.do({arg item, i;
						// var sw = 0;
						// if((item == 1).and(~buttsCatLPs2.includes(i + 1) == false), {
							// ~buttsCatLPs2.do({arg cont, n;
								// switch(sw,
									// 0, {if(cont == 0, {~buttsCatLPs2[n] = i + 1; sw = 1})},
									// 1, {~buttsCatLPs2[n].postln})});
							// sw = 0});
					// });
					// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
					// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
// 
// 
					// ~lprecbutnum2[i] = 0;
					// ~recorder2.free;
					// ~pitcher2.free;
					// ~predisk2.close;
					// ~predisk2.free;
					// ~recFileName2.free;
				// };
			// });
		// });
// 
		// if(state[1].nnToggle.count({arg n; n == 1}) < 4, {
// 
			// if((note >= 36).and(note <= 51), {
// 
				// switch(state[1].nnToggle[note],
					// 0, {midiOut.noteOn(0, note, 48); state[1].nnToggle[note] = 1;
// 
						// state[1].nnToggle.do({arg item, i;
							// var sw = 0;
							// if((item == 1).and(~buttsCatLPs2.includes(i + 1) == false), {
								// ~buttsCatLPs2.do({arg cont, n;
									// switch(sw,
										// 0, {if(cont == 0, {~buttsCatLPs2[n] = i + 1; sw = 1})},
										// 1, {~buttsCatLPs2[n].postln})});
								// sw = 0});
						// });
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
					// },
					// 1, {midiOut.noteOn(0, note, 16); state[1].nnToggle[note] = 0;
						// ~buttsCatLPs2.do({arg item, i; if(state[1].nnToggle[item-1] == 0, {~buttsCatLPs2[i] = 0})});
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
				// });
			// });
// 
			// if((note >= 52).and(note <= 67), {
				// switch(state[1].nnToggle[note],
					// 0, {midiOut.noteOn(0, note, 48); state[1].nnToggle[note] = 1;
						// state[1].nnToggle.do({arg item, i;
							// var sw = 0;
							// if((item == 1).and(~buttsCatLPs2.includes(i + 1) == false), {
								// ~buttsCatLPs2.do({arg cont, n;
									// switch(sw,
										// 0, {if(cont == 0, {~buttsCatLPs2[n] = i + 1; sw = 1})},
										// 1, {~buttsCatLPs2[n].postln});
								// });
								// sw = 0});
						// });
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
					// },
					// 1, {midiOut.noteOn(0, note, 17); state[1].nnToggle[note] = 0;
						// ~buttsCatLPs2.do({arg item, i; if(state[1].nnToggle[item-1] == 0, {~buttsCatLPs2[i] = 0})});
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
				// });
			// });
// 
			// if((note >= 68).and(note <= 83), {
				// switch(state[1].nnToggle[note],
					// 0, {midiOut.noteOn(0, note, 48); state[1].nnToggle[note] = 1;
						// state[1].nnToggle.do({arg item, i;
							// var sw = 0;
							// if((item == 1).and(~buttsCatLPs2.includes(i + 1) == false), {
								// ~buttsCatLPs2.do({arg cont, n;
									// switch(sw,
										// 0, {if(cont == 0, {~buttsCatLPs2[n] = i + 1; sw = 1})},
										// 1, {~buttsCatLPs2[n].postln})});
								// sw = 0});
						// });
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
					// },
					// 1, {midiOut.noteOn(0, note, 1); state[1].nnToggle[note] = 0;
						// ~buttsCatLPs2.do({arg item, i; if(state[1].nnToggle[item-1] == 0, {~buttsCatLPs2[i] = 0})});
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
				// });
			// });
// 
			// if((note >= 84).and(note <= 99), {
				// switch(state[1].nnToggle[note],
					// 0, {midiOut.noteOn(0, note, 48); state[1].nnToggle[note] = 1;
						// state[1].nnToggle.do({arg item, i;
							// var sw = 0;
							// if((item == 1).and(~buttsCatLPs2.includes(i + 1) == false), {
								// ~buttsCatLPs2.do({arg cont, n;
									// switch(sw,
										// 0, {if(cont == 0, {~buttsCatLPs2[n] = i + 1; sw = 1})},
										// 1, {~buttsCatLPs2[n].postln})});
								// sw = 0});
						// });
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
					// },
					// 1, {midiOut.noteOn(0, note, 16); state[1].nnToggle[note] = 0;
						// ~buttsCatLPs2.do({arg item, i; if(state[1].nnToggle[item-1] == 0, {~buttsCatLPs2[i] = 0})});
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
				// });
			// });
			// },
// 
			// {
				// if((note >= 36).and(note <= 51).and(state[1].nnToggle[note] == 1),
					// {midiOut.noteOn(0, note, 16); state[1].nnToggle[note] = 0;
						// ~buttsCatLPs2.do({arg item, i; if(state[1].nnToggle[item-1] == 0, {~buttsCatLPs2[i] = 0})});
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
				// });
// 
				// if((note >= 52).and(note <= 67).and(state[1].nnToggle[note] == 1),
					// {midiOut.noteOn(0, note, 17); state[1].nnToggle[note] = 0;
						// ~buttsCatLPs2.do({arg item, i; if(state[1].nnToggle[item-1] == 0, {~buttsCatLPs2[i] = 0})});
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
				// });
// 
				// if((note >= 68).and(note <= 83).and(state[1].nnToggle[note] == 1),
					// {midiOut.noteOn(0, note, 1); state[1].nnToggle[note] = 0;
						// ~buttsCatLPs2.do({arg item, i; if(state[1].nnToggle[item-1] == 0, {~buttsCatLPs2[i] = 0})});
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
				// });
// 
				// if((note >= 84).and(note <= 99).and(state[1].nnToggle[note] == 1),
					// {midiOut.noteOn(0, note, 16); state[1].nnToggle[note] = 0;
						// ~buttsCatLPs2.do({arg item, i; if(state[1].nnToggle[item-1] == 0, {~buttsCatLPs2[i] = 0})});
						// ~sampleNumber2 = ~buttsCatLPs2.as(Array);
						// ~sampleNumber2.do{| item, i | if( item == 0) {~sampleNumber2.do{| a | if(a != 0) {~sampleNumber2[i] = a}}}};
				// });
		// });
	// });
// 
	// if((currentPageIndex == 1).and(state[1].isArm).and(isModeStepSequencer.not).and(isModeStepSequencerEdit.not).and(
		// ~lprecbutnum2.includes(1) == false).and(note >= 36).and(note <= 99).and(
			// (state[1].nnToggle.count({arg n; n == 1}) < 4).or(state[1].nnToggle[note] == 1)),
// 
		// {
			// ~recFileName2 = recordingDirectory +/+ ("Recording-" ++ thisThread.seconds.asString).replace(".", "-") ++ ".wav";
			// ~predisk2 = Buffer.alloc(Server.default, 65536, 1);
			// ~predisk2.write(~recFileName2, "wav", "int16", 0, 0, true);
			// ~recorder2 = Synth.tail(nil, "recorder", ["bufnum", ~predisk2]);
			// ~pitcher2 = Synth("pitchtector2");
			// midiOut.noteOn(0, note, 15);
			// ~lprecbutnum2[note] = 1;
	// });
// 
	// controlSynth.set(\b5, ~samples2[~sampleNumber2[0]].bufnum);
	// controlSynth.set(\b6, ~samples2[~sampleNumber2[1]].bufnum);
	// controlSynth.set(\b7, ~samples2[~sampleNumber2[2]].bufnum);
	// controlSynth.set(\b8, ~samples2[~sampleNumber2[3]].bufnum);
// 
	// controlSynth.set(\p5, state[1].pitches[~sampleNumber2[0]]);
	// controlSynth.set(\p6, state[1].pitches[~sampleNumber2[1]]);
	// controlSynth.set(\p7, state[1].pitches[~sampleNumber2[2]]);
	// controlSynth.set(\p8, state[1].pitches[~sampleNumber2[3]]);
// 
	// if(~unpitched.includes(~sampleNumber2[0]), {controlSynth.set(\st5, 0)}, {controlSynth.set(\st5, 1)});
	// if(~unpitched.includes(~sampleNumber2[1]), {controlSynth.set(\st6, 0)}, {controlSynth.set(\st6, 1)});
	// if(~unpitched.includes(~sampleNumber2[2]), {controlSynth.set(\st7, 0)}, {controlSynth.set(\st7, 1)});
	// if(~unpitched.includes(~sampleNumber2[3]), {controlSynth.set(\st8, 0)}, {controlSynth.set(\st8, 1)});
	// // ~buttsCatLPs2.postln;
	// // ~sampleNumber2.postln;
// 
        // }, srcID: deviceID);
// 
// // ------------------------------------------------------------------------- SEQUENCER
// 
// ~stepsNum = 120;
// ~seqSize = 64;
// ~modebtns = Array.newClear(8).seriesFill(8, 16).collect({arg n; n + (0..7)}).flat;
// ~sequence = Array.fill(127, 0);
// ~seqRoutInternal = Array.newClear(127);
// ~seqRoutInternalFiltered = ~seqRoutInternal.as(Array);
// ~sequenceToSynth = Array.newClear(127);
// ~seqCursor = (0..127);
// ~seqCursorFiltered = ~seqCursor.as(Array);
// ~routineStep = 0;
// ~ledDelay = 0;
// 
// ~seqLedFeedback = {
	// ~sequenceToSynth = ~sequence.as(Array);
	// ~modebtns.do({arg i, n;
		// if(~sequenceToSynth.size > (i - n), {~sequenceToSynth.removeAt(i - n)});
	// });
// 
	// ~seqRoutInternalFiltered = ~seqRoutInternal.as(Array);
	// ~modebtns.do({arg i, n;
		// if(~seqRoutInternalFiltered.size > (i - n), {~seqRoutInternalFiltered.removeAt(i - n)});
	// });
// 
	// ~seqCursorFiltered = ~seqRoutInternal.as(Array);
	// ~seqCursorFiltered.do({arg i, n;
		// if(i.isNumber, {~seqCursorFiltered[n] = nil},
			// {~seqCursorFiltered[n] = n});
	// });
// 
	// ~modebtns.do({arg i, n;
		// if(~seqCursorFiltered.size > (i - n), {~seqCursorFiltered.removeAt(i - n)});
	// });
// };
// 
// ~seqLedFeedback.value;
// 
// ~r1 = Routine.run({
	// loop{
		// ~sequenceToSynth.lace(~seqSize).rotate(~routineStep).do({arg i, n;
			// if(i == 1, {controlSynth.set(\trigger, 1000.rand)});
			// ~routineStepExchange = n + 1;
			// if((state[1].isOctRandomised[0]).and(state[1].isOctRandomised[1].not),
				// {controlSynth.set(\roct2, [1, 2, 4].choose)});
			// if((state[1].isOctRandomised[1]).and(state[1].isOctRandomised[0].not),
				// {controlSynth.set(\roct2, [0.25, 0.5, 1].choose)});
			// if((state[1].isOctRandomised[0]).and(state[1].isOctRandomised[1]),
				// {controlSynth.set(\roct2, [0.25, 0.5, 1, 2, 4].choose)});
// 
			// ~seqNoteDurCurrent.wait;
		// });
	// };
// }, clock: t);
// 
// ~r2 = Routine.run({
	// loop{
		// ~seqRoutInternalFiltered.lace(~seqSize).rotate(~routineStep).do({arg i, n;
			// if((i.isNumber).and(isModeStepSequencer).and(currentPageIndex == 1), {midiOut.noteOn(0, i, 27)});
			// ~seqNoteDurCurrent.wait;
		// });
	// };
// }, clock: t);
// 
// ~r3 = Routine.run({
	// loop{
		// var note = ~seqRoutInternalFiltered.lace(~seqSize).rotate(~routineStep).wrapAt(-1);
// 
		// if((note.isNumber).and(isModeStepSequencer).and(~modebtns.includes(note) == false).and(currentPageIndex == 1),
			// {midiOut.noteOn(0, note, 60)});
// 
		// ~seqRoutInternalFiltered.lace(~seqSize).rotate(~routineStep).do({arg i, n;
// 
			// ~seqNoteDurCurrent.wait;
			// if((i.isNumber).and(isModeStepSequencer).and(currentPageIndex == 1), {midiOut.noteOn(0, i, 60)});
		// });
	// };
// }, clock: t);
// 
// ~r4 = Routine.run({
	// loop{
		// ~seqCursorFiltered.lace(~seqSize).rotate(~routineStep).do({arg i, n;
			// if((i.isNumber).and(isModeStepSequencer).and(currentPageIndex == 1), {midiOut.noteOn(0, i, 58)});
			// ~seqNoteDurCurrent.wait;
		// });
	// };
// }, clock: t);
// 
// ~r5 = Routine.run({
	// loop{
		// var note = ~seqCursorFiltered.lace(~seqSize).rotate(~routineStep).wrapAt(-1);
// 
		// if((note.isNumber).and(isModeStepSequencer).and(~modebtns.includes(note) == false).and(currentPageIndex == 1),
			// {midiOut.noteOn(0, note, 16)});
// 
		// ~seqCursorFiltered.lace(~seqSize).rotate(~routineStep).do({arg i, n;
			// ~seqNoteDurCurrent.wait;
			// if((i.isNumber).and(isModeStepSequencer).and(currentPageIndex == 1), {midiOut.noteOn(0, i, 16)});
		// });
	// };
// }, clock: t);
// 
// MIDIFunc.noteOn({arg vel, note, ch, id;
// 
	// Routine.run({
		// if((currentPageIndex == 1).and(
			// (
				// (note == 8).and(isModeStepSequencer)
                // ).or(
                // (note == 24).and(isModeStepSequencerEdit)
			// )).and(isCurrentLayoutDrumRack.not), {
				// midiOut.control(0, 0, 2); isCurrentLayoutDrumRack = true;
				// midiOut.noteOn(0, 100, 30);
				// midiOut.noteOn(0, 101, 30);
// 
				// if(state[1].isArm.not, {
					// animations.arm.stop;
					// midiOut.noteOn(0, 107, 16);
					// (52..67).do({arg n; midiOut.noteOn(0, n, 17)});
					// (84..99).do({arg n; midiOut.noteOn(0, n, 16)});
					// (36..51).do({arg n; midiOut.noteOn(0, n, 16)});
					// (68..83).do({arg n; midiOut.noteOn(0, n, 1)});
					// state[1].nnToggle.do({arg item, i;
						// if(item == 1, {midiOut.noteOn(0, i + 36, 48)});
					// });
				// });
// 
				// if(state[1].isArm, {
					// animations.arm.play;
					// (52..67).do({arg n; midiOut.noteOn(0, n, 17)});
					// (84..99).do({arg n; midiOut.noteOn(0, n, 16)});
					// (36..51).do({arg n; midiOut.noteOn(0, n, 16)});
					// (68..83).do({arg n; midiOut.noteOn(0, n, 1)});
					// ~lprecbutnum2.do({arg item, i;
						// if(item == 1, {midiOut.noteOn(0, i + 36, 15)});
					// });
					// state[1].nnToggle.do({arg item, i;
						// if((item == 1).and(~lprecbutnum2[i] != 1), {midiOut.noteOn(0, i + 36, 48)});
					// });
				// });
// 
				// isModeStepSequencer = false;
				// isModeStepSequencerEdit = false;});
// 
                // if((currentPageIndex == 1).and(
                    // isCurrentLayoutDrumRack.not).and(isModeStepSequencer).and(
				// ~modebtns.includes(note) == false).and(note < ~stepsNum), {
				// var flash;
// 
				// flash = Routine.run({
					// loop{
						// if((currentPageIndex == 1).and(isCurrentLayoutDrumRack.not).and(isModeStepSequencer), {
							// midiOut.noteOn(0, note, 60);
						// });
						// 0.1.wait;
						// if((currentPageIndex == 1).and(isCurrentLayoutDrumRack.not).and(isModeStepSequencer), {
							// midiOut.noteOn(0, note, 16);
						// });
						// 0.1.wait;
					// };
				// });
// 
				// ~ledDelay = ~ledDelay + ~seqNoteDurCurrent;
// 
				// Routine({
					// ~ledDelay.wait;
// 
					// Routine({
						// switch(~sequence[note],
							// 0, {~sequence[note] = 1; ~seqRoutInternal[note] = note;
								// if((currentPageIndex == 1).and(isCurrentLayoutDrumRack.not).and(isModeStepSequencer), {
									// midiOut.noteOn(0, note, 60)});
							// },
							// 1, {~sequence[note] = 0; ~seqRoutInternal[note] = nil;
								// if((currentPageIndex == 1).and(isCurrentLayoutDrumRack.not).and(isModeStepSequencer), {
									// midiOut.noteOn(0, note, 16)});
						// });
// 
						// ~routineStep = ~routineStep - ~routineStepExchange;
// 
						// ~r1.reset; ~r2.reset; ~r3.reset; ~r4.reset; ~r5.reset; flash.stop;
// 
						// ~seqLedFeedback.value;
						// ~ledDelay = ~ledDelay - ~seqNoteDurCurrent;
					// }).play(t, ~seqNoteDurCurrent);
				// }).play(t);
		// });
// 
		// s.wait;
// 
        // if((currentPageIndex == 1).and( 
            // ((note == 100).and(isCurrentLayoutDrumRack)).or(
                // (note == 8).and(isCurrentLayoutDrumRack.not)))	, {
// 
				// midiOut.control(0, 0, 1); isCurrentLayoutDrumRack = false;
				// midiOut.noteOn(0, 8, 63); isModeStepSequencer = true;
				// midiOut.noteOn(0, 24, 30); isModeStepSequencerEdit = false;
// 
				// ~stepsNum.do({arg n;
					// if(~modebtns.includes(n) == false, {
						// midiOut.noteOn(0, n, 16)});
				// });
// 
				// if((~stepsNum != 120).and(~stepsNum != 0), {
					// (~stepsNum..120).do({arg n;
						// if(~modebtns.includes(n) == false, {
							// midiOut.noteOn(0, n, 0);
						// });
					// });
				// });
// 
				// ~sequence.lace(~stepsNum).do({arg i, n; if(i == 1, {midiOut.noteOn(0, n, 60)})});
// 
				// ~seqLedFeedback.value;
		// });
// 
	// });
// 
	// Routine.run({
// 
// if((currentPageIndex == 1).and(
// ~modebtns.includes(note) == false).and(isCurrentLayoutDrumRack.not).and(isModeStepSequencerEdit), {
				// ~stepsNum = note + 1;
// 
				// ~stepsNum.do({arg n;
					// if(~modebtns.includes(n) == false, {
						// midiOut.noteOn(0, n, 16);
					// });
				// });
// 
				// if((~stepsNum != 120).and(~stepsNum != 0), {
					// (~stepsNum..120).do({arg n;
						// if(~modebtns.includes(n) == false, {
							// midiOut.noteOn(0, n, 0);
						// });
					// });
				// });
// 
				// ~seqSize = (((~stepsNum.roundUp(8) / 2) - 4) + ((~stepsNum - 1)%8 + 1)).asInteger;
// 
				// ~r1.reset; ~r2.reset; ~r3.reset; ~r4.reset; ~r5.reset; ~stepCounter.reset;
// 
				// ("Steps number changed to: " + ~seqSize.asString).postln;
		// });
// 
		// s.wait;
// 
		// if((currentPageIndex == 1).and(
			// ((note == 24).and(isCurrentLayoutDrumRack.not)).or((note == 101).and(isCurrentLayoutDrumRack.not))), {
				// midiOut.control(0, 0, 1); isCurrentLayoutDrumRack = false;
				// isModeStepSequencerEdit = true; midiOut.noteOn(0, 24, 63);
				// isModeStepSequencer = false; midiOut.noteOn(0, 8, 30);
				// ~stepsNum.do({arg n;
					// if(~modebtns.includes(n) == false, {
						// midiOut.noteOn(0, n, 16);
					// });
				// });
// 
				// if((~stepsNum != 120).and(~stepsNum != 0), {
					// (~stepsNum..120).do({arg n;
						// if(~modebtns.includes(n) == false, {
							// midiOut.noteOn(0, n, 0);
						// });
					// });
				// });
		// });
	// });
// 
	// // Durations
    // if((
    // (currentPageIndex == 0).or(currentPageIndex == 1)
		// ).and(isCurrentLayoutDrumRack).and((102..106).includes(note)), {
			// midiOut.noteOn(0, note, 63);
			// (102..106).do({arg i, n;
				// if(note == i, {~seqNoteDurCurrent = ~seqNoteDurs[n]},
					// {midiOut.noteOn(0, i, 29)});
			// });
	// });
// 
// if((
// (isModeStepSequencer).or(isModeStepSequencerEdit)
		// ).and(isCurrentLayoutDrumRack.not).and([40, 56, 72, 88, 104].includes(note)), {
			// midiOut.noteOn(0, note, 63);
			// [40, 56, 72, 88, 104].do({arg i, n;
				// if(note == i, {~seqNoteDurCurrent = ~seqNoteDurs[n]},
					// {midiOut.noteOn(0, i, 29)});
			// });
	// });
        // }, srcID: deviceID);
	}

	initLoggerTextFields {
		AppClock.sched(0.0, {
			loggerTextFields.do({arg tf, n;
				tf.enabled = false;
				tf.string = "Sampler" + (n+1);
			});
		});
	}

    free {
        midiFuncs.do({arg midiFunc;
            midiFunc.free();
        });
    }
}
