SynthDef.synthDefDir = "~/Desktop/Iannis/synths".standardizePath;

s.boot;
//
// Granulator
//
// ~metadata = (
  // type: \synth,
  // name: "Granulator",
  // author: "Ales Tsurko",
  // site: "http://alestsurko.by",
  // description: "Granular sampler.",
  // ui: (
    // pages: [
      // // каждая страница -- отдельный таб
      // (
        // name: "OSC 1",
        // groups: [
          // (
            // // если параметра name нет, то группа не будет
            // // выделяться как группа
            // // параметр name может быть пустой строкой "",
            // // тогда группа будет визуально группой, но
            // // без имени
            // bufnum: (
              // name: "Sample",
              // spec: \bufnum,
              // ui: (
                // type: \recorder
              // )
            // )
          // ),
          // (
            // name: "Grain Parameters",
            // type: (
              // name: "Type",
              // // spec: ...,
              // ui: (
                // type: \switch_view,
                // items: [
                  // // Type I (Warp1)
                  // (
                    // name: "I",
                    // rows: [(
                      // align: \center,
                      // items: [
                        // (
                          // grain_dur: (
                            // name: "Duration",
                            // // spec: ...,
                            // ui: (
                              // type: \knob
                            // )
                          // )
                        // ),
                        // (
                          // pos: (
                            // name: "Position",
                            // spec: \unipolar,
                            // ui: (
                              // type: \knob
                            // )
                          // )
                        // ),
                        // (
                          // density: (
                            // name: "Density",
                            // // spec: ...,
                            // ui: (
                              // type: \knob
                            // )
                          // )
                        // ),
                        // (
                          // t1rand: (
                            // name: "Randomness",
                            // spec: \unipolar,
                            // ui: (
                              // type: \knob
                            // )
                          // )
                        // )
                      // ]
                    // )]
                  // ),
// 
                  // // Type II (TGrains3)
                  // (
                    // name: "II",
                    // rows: [(
                      // align: \center,
                      // items: [
                        // (
                          // grain_dur: (
                            // name: "Duration",
                            // // spec: ...,
                            // ui: (
                              // type: \knob
                            // )
                          // )
                        // ),
                        // (
                          // pos: (
                            // name: "Position",
                            // spec: \unipolar,
                            // ui: (
                              // type: \knob
                            // )
                          // )
                        // ),
                        // (
                          // t2gatt: (
                            // name: "Grain Attack",
                            // // spec: ...,
                            // ui: (
                              // type: \knob
                            // )
                          // ),
                          // t2gdec: (
                            // name: "Grain Decay",
                            // // spec: ...,
                            // ui: (
                              // type: \knob
                            // )
                          // )
                        // )
                      // ]
                    // )],
// 
                    // trig: (
                      // name: "Trigger Sequencer",
                      // // spec: ...,
                      // ui: (
                        // type: \matrix_sequencer
                      // )
                    // )
                  // ),
                // ]
              // ),
            // ),
          // ),
          // (
            // name: "Envelope",
            // env: (
              // name: "Envelope",
              // // spec: ...,
              // ui: (
                // type: \env_adsr
              // )
            // )
          // ),
          // (
            // name: "Output",
            // row: (
              // align: \right,
              // items: [
                // amp: (
                  // name: "Amp",
                  // spec: \amp,
                  // ui: (
                    // type: \hslider
                  // )
                // ),
                // pan: (
                  // name: "Pan",
                  // spec: \pan,
                  // ui: (
                    // type: \hslider
                  // )
                // )
              // ]
            // )
          // )
        // ]
      // )
    // ]
  // ),
  // factory_presets: [
    // (
      // name: "Default",
      // values: ()
    // )
  // ]
// );

~metadata = (
  type: \synth,
  name: "Granulator",
  author: "Ales Tsurko",
  site: "http://alestsurko.by",
  description: "Granular sampler.",
  ui: (
    pages: [
      // каждая страница -- отдельный таб
      (
        name: "OSC 1",
        groups: [
          // Bufnum group
          (
            // если параметра name нет, то группа не будет
            // выделяться как группа
            // параметр name может быть пустой строкой "",
            // тогда группа будет визуально группой, но
            // без имени
            parameters: [(
              key: \bufnum,
              ui: (
                type: \recorder
              )
            )]
          ),

          // Grain Parameters Group
          (
            name: "Grain Parameters",
            parameters: [(
              key: \grain_env_bufnum,
              name: "Grain Envelope",
              ui: (
                type: \popup,
                align: \left,
                items: [
                  (
                    name: "Rectangle",
                    value: {
                      var buffer, signal;
                      var bufSize = 8192;
                      buffer = Buffer.alloc(Server.default, bufSize, 1);
                      signal = Signal.rectWindow(bufSize);
                      buffer.loadCollection(signal);

                      // return
                      buffer.bufnum;
                    }
                  ),
                  (
                    name: "Hanning",
                    value: {
                      var buffer, signal;
                      var bufSize = 8192;
                      buffer = Buffer.alloc(Server.default, bufSize, 1);
                      signal = Signal.hanningWindow(bufSize);
                      buffer.loadCollection(signal);

                      // return
                      buffer.bufnum;
                    }
                  ),
                  (
                    name: "Hamming",
                    value: {
                      var buffer, signal;
                      var bufSize = 8192;
                      buffer = Buffer.alloc(Server.default, bufSize, 1);
                      signal = Signal.hammingWindow(bufSize);
                      buffer.loadCollection(signal);

                      // return
                      buffer.bufnum;
                    }
                  ),
                  (
                    name: "Welch",
                    value: {
                      var buffer, signal;
                      var bufSize = 8192;
                      buffer = Buffer.alloc(Server.default, bufSize, 1);
                      signal = Signal.welchWindow(bufSize);
                      buffer.loadCollection(signal);

                      // return
                      buffer.bufnum;
                    }
                  )
                ]
              )
            ),
            (
              // parameters row
              isRow: 0,
              align: \center,

              parameters: [(
                key: \grain_dur,
                name: "Duration",
                spec: ControlSpec(0.05, 3, \exp, 0, 0.1, "s"),
                ui: (
                  type: \knob
                )
              ),
              (
                key: \pos,
                name: "Position",
                spec: \unipolar,
                ui: (
                  type: \knob
                )
              ),
              (
                key: \density,
                name: "Density",
                spec: ControlSpec(1, 16, \lin, 1, 1),
                ui: (
                  type: \knob
                )
              ),
              (
                key: \t1rand,
                name: "Randomness",
                spec: \unipolar,
                ui: (
                  type: \knob
                )
              )]
            )
          ]),

          // Envelope Group
          (
            name: "Envelope",
            parameters: [(
              key: \env,
              name: "Envelope",
              ui: (
                type: \env_adsr
              )
            )]
          ),

          // Output group
          (
            name: "Output",
            parameters: [(
              isRow: 0,
              align: \right,

              parameters: [(
                key: \amp,
                name: "Amp",
                spec: \amp,
                ui: (
                  type: \hslider
                )
              ),
              (
                key: \pan,
                name: "Pan",
                spec: \pan,
                ui: (
                  type: \hslider
                )
              )]
            )]
          )
        ]
      )
    ]
  ),
  factory_presets: [(
    name: "Default",
    values: ()
  )]
);

s.boot();
v = IannisSynthViewController(nil, ~metadata);
v.parse();
v.front();

SynthDef("granulator", {arg bufnum, gran_env_bufnum = -1, 
  gran_dur = 1, pos = 0.0, density = 4, t1rand = 0.1,
  // trig, t2gpan = 0, t2gamp = 1, t2gatt, t2gdec,
  env = #[0.005, 0.01, 1, 0.1], amp = 0.9, pan = 0.0, freq, gate = 1, out = 0;

  var envelope = EnvGen.kr(Env.adsr(env[0], env[1], env[2], env[3]), gate, doneAction: 2);
  var note = (freq.cpsmidi-60).midiratio;
  var output = Warp1.ar(1, bufnum, pos, note*BufRateScale.kr(bufnum), gran_dur, gran_env_bufnum, density, t1rand, 4);
	// Note: Unlike PlayBuf, the TGrains3 rate is multiplied by BufRate, so you needn't do that yourself.
	// var tgrains = TGrains3.ar(2, trig, bufnum, note, BufDur.kr(bufnum)*pos, grain_dur, t2gpan, t2gatt, t2gdec, 1, 4);
  // var output = Select.ar(type, [warp, tgrains]); // здесь выбор в соответствии с типом синтеза
  output = output * envelope;

  Out.ar(out, Pan2.ar(output, pan, amp));
}, [\ir, \ir,
	0, 0, 0, 0, 
	nil, 0.01, 0.01, 0, nil, \ir],
	metadata: ~metadata
).writeDefFile(SynthDef.synthDefDir);
