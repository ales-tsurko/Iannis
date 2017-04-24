(
SynthDef.synthDefDir = "~/Desktop/Iannis/synths".standardizePath;

s.boot;
//
// Granulator
//

~metadata = (
  type: \synth,
  name: "Granulator",
  id: "by.alestsurko.granulator",
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
              key: \gran_env_bufnum,
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
                      signal = Signal.rectWindow(bufSize);
                      buffer = Buffer.loadCollection(Server.default, signal);

                      // return
                      buffer.bufnum;
                    }
                  ),
                  (
                    name: "Hanning",
                    value: -1
                  ),
                  (
                    name: "Hamming",
                    value: {
                      var buffer, signal;
                      var bufSize = 8192;
                      signal = Signal.hammingWindow(bufSize);
                      buffer = Buffer.loadCollection(Server.default, signal);

                      // return
                      buffer.bufnum;
                    }
                  ),
                  (
                    name: "Welch",
                    value: {
                      var buffer, signal;
                      var bufSize = 8192;
                      signal = Signal.welchWindow(bufSize);
                      buffer = Buffer.loadCollection(Server.default, signal);

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
                key: \gran_dur,
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
                key: \gain,
                name: "Gain",
                spec: ControlSpec(0.ampdb, 24, \db, units: "dB"),
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
  presets: [
    (
      name: "Default",
      isFactory: true,
      values: (
        // bufnum: "here/should/be/a/path/to/a/file",
        env: [0.1, 0.3, 0.5, 1, -1, 0, -2],
        gran_env_bufnum: 1, // popup menu item index
        gran_dur: 1,
        pos: 0,
        density: 4,
        t1rand: 0.1,
        gain: 0.0,
        pan: 0
      )
    ),
    (
      name: "Another Preset",
      isFactory: true,
      values: (
        env: [2, 0.5, 0.3, 4, -1, 0, -2],
        gran_env_bufnum: 0, // popup menu item index
        gran_dur: 2,
        pos: 0.1,
        density: 1,
        t1rand: 0,
        gain: 0.0,
        pan: 0.1
      )
    ),
    (
      name: "Preset 3",
      isFactory: true,
      values:
      (
        env: [0.05, 1, 1, 0.5, 0, 0, 2],
        gran_env_bufnum: 2, // popup menu item index
        gran_dur: 0.1,
        pos: 0.5,
        density: 8,
        t1rand: 0.7,
        gain: 0.0,
        pan: {rrand(-1.0, 1.0)}
      )
    )
  ]
);
)

SynthDef("granulator", {arg bufnum, gran_env_bufnum = -1, 
  gran_dur = 1, pos = 0.0, density = 4, t1rand = 0.1,
  // trig, t2gpan = 0, t2gamp = 1, t2gatt, t2gdec,
  env = #[0.005, 0.01, 1, 0.1, 0, 0, 0], gain = 0, pan = 0.0, velocity, freq, gate = 1, out = 0;

  var veloamp = velocity/127;
  var note = (freq.cpsmidi-60).midiratio;
  var envelope = EnvGen.kr(Env.adsr(env[0], env[1], env[2], env[3], 1, env[4..6]), gate, doneAction: 2);
  var output = Warp1.ar(1, bufnum, pos, note*BufRateScale.kr(bufnum), gran_dur, gran_env_bufnum, density, t1rand, 4);
	// Note: Unlike PlayBuf, the TGrains3 rate is multiplied by BufRate, so you needn't do that yourself.
	// var tgrains = TGrains3.ar(2, trig, bufnum, note, BufDur.kr(bufnum)*pos, grain_dur, t2gpan, t2gatt, t2gdec, 1, 4);
  // var output = Select.ar(type, [warp, tgrains]); // здесь выбор в соответствии с типом синтеза
  output = output * envelope * veloamp;

  Out.ar(out, Pan2.ar(output, pan, gain.dbamp));
}, [\ir, nil,
	0, 0, 0, 0, 
	0, 0.01, 0.01, 0, 0, nil, \ir],
	metadata: ~metadata
).writeDefFile(SynthDef.synthDefDir);
