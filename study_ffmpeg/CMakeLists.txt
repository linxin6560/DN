cmake_minimum_required(VERSION 3.4.1)
set(CPP_DIR src/main/cpp)
add_subdirectory(src/main/cpp/librtmp)
file(GLOB my_source src/main/cpp/*.cpp)
MESSAGE("CPP_DIR路径  " ${CPP_DIR})
add_library( native-lib
             SHARED
             ${my_source})
find_library( log-lib
              log )
include_directories(libs/include)
set(DIR ../../../../libs)
MESSAGE("路径  " ${DIR})
add_library(x264
            STATIC
            IMPORTED)
set_target_properties(x264
                      PROPERTIES IMPORTED_LOCATION
                      ${DIR}/armeabi/libx264.a)
add_library(faac
            STATIC
            IMPORTED)
set_target_properties(faac
                      PROPERTIES IMPORTED_LOCATION
                      ${DIR}/armeabi/libfaac.a)
add_library(avcodec-56
            SHARED
            IMPORTED)
set_target_properties(avcodec-56
                      PROPERTIES IMPORTED_LOCATION
                      ${DIR}/armeabi/libavcodec-56.so)
add_library(avdevice-56
            SHARED
            IMPORTED)
set_target_properties(avdevice-56
                      PROPERTIES IMPORTED_LOCATION
                      ${DIR}/armeabi/libavdevice-56.so)
add_library(avformat-56
            SHARED
            IMPORTED)
set_target_properties(avformat-56
                      PROPERTIES IMPORTED_LOCATION
                      ${DIR}/armeabi/libavformat-56.so)
add_library(avutil-54
            SHARED
            IMPORTED)
set_target_properties(avutil-54
                      PROPERTIES IMPORTED_LOCATION
                      ${DIR}/armeabi/libavutil-54.so)
add_library(postproc-53
            SHARED
            IMPORTED)
set_target_properties(postproc-53
                      PROPERTIES IMPORTED_LOCATION
                      ${DIR}/armeabi/libpostproc-53.so)
add_library(swresample-1
             SHARED
             IMPORTED)
set_target_properties(swresample-1
                       PROPERTIES IMPORTED_LOCATION
                       ${DIR}/armeabi/libswresample-1.so)
add_library(swscale-3
              SHARED
              IMPORTED)
set_target_properties(swscale-3
                        PROPERTIES IMPORTED_LOCATION
                        ${DIR}/armeabi/libswscale-3.so)
add_library(avfilter-5
              SHARED
              IMPORTED)
set_target_properties(avfilter-5
                        PROPERTIES IMPORTED_LOCATION
                        ${DIR}/armeabi/libavfilter-5.so)
target_link_libraries( native-lib
                       x264
                       faac
                       rtmp
                       avcodec-56
                       avdevice-56
                       avformat-56
                       avutil-54
                       postproc-53
                       swresample-1
                       swscale-3
                       avfilter-5
                       android
                       OpenSLES
                       ${log-lib} )
