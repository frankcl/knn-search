#ifndef XIN_MANONG_KNN_COMMON_MACRO_H
#define XIN_MANONG_KNN_COMMON_MACRO_H

#define BEGIN_NAMESPACE(n) namespace n {
#define END_NAMESPACE(n) }

#define DELETE_AND_SET_NULL(object) \
    do { delete object; object = NULL; } while(0)

#define DELETE_ARRAY_AND_SET_NULL(object) \
    do { delete[] object; object = NULL; } while(0)

#endif
