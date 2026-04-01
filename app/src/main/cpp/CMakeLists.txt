cmake_minimum_required(VERSION 3.22.1)
project("ollama.mobile.native")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find Android NDK
include_directories(${CMAKE_CURRENT_SOURCE_DIR})

# Add llama.cpp as a subdirectory
# For now, we'll create a stub that can be replaced with actual llama.cpp
add_library(llama SHARED 
    llama_jni.cpp
    llama_backend.cpp
)

# Link required libraries
target_link_libraries(llama 
    android
    log
)
