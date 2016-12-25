
cmake_minimum_required(VERSION 3.4.1)

file( GLOB srcfiles
      "src/main/cpp/*.cpp"
      "src/main/cpp/emu/*.cpp"
      "src/main/cpp/emu/pc/*.cpp"
      "src/main/cpp/emu/rom/*.cpp"
)

add_library( Droid64 SHARED ${srcfiles} )
