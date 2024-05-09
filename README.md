# Simple MCRTX PostFX Shader

Just a basic template shader written from scratch for Bedrock RTX that modifies PostFX materials (bloom and tonemapping).

## Compilation

Add vanilla bloom and tonemapping material.bin files into the `vanilla` folder and compile with [lazurite](https://github.com/veka0/lazurite) by running the following command:

```sh
lazurite build ./src
```

Note that compiling BGFX shaders requires a Shaderc compiler executable, see more information in the [build command](https://veka0.github.io/lazurite/commands/#build) description
