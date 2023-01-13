#ifndef XIN_MANONG_KNN_COMMON_STRINGUTIL_H
#define XIN_MANONG_KNN_COMMON_STRINGUTIL_H

#include <exception>
#include <iostream>
#include <string>
#include <sstream>
#include <vector>
#include "knn/common/Macro.h"

BEGIN_NAMESPACE(xin)
BEGIN_NAMESPACE(manong)
BEGIN_NAMESPACE(knn)
BEGIN_NAMESPACE(common)

class StringUtil {
public:
    static bool strToInt32(const std::string& str, int32_t& v);
    static bool strToUInt32(const std::string& str, uint32_t& v);    
    static bool strToInt64(const std::string& str, int64_t& v);
    static std::string int64ToStr(const int64_t v);
    static std::string int32ToStr(const int32_t v);
    static std::string uint32ToStr(const uint32_t v);    
    static void trim(std::string& str);
    static std::vector<std::string> split(const std::string& str, const std::string& splitStr);
};

inline bool StringUtil::strToInt32(const std::string& str, int32_t& v) {
    try {
        std::stringstream ss;
        ss << str;
        ss >> v;
        if (ss.fail()) return false;
        return true;
    } catch (std::exception& e) {
        std::cerr << "convert string[" << str << "] to int32_t failed" << std::endl;
        return false;
    }
}

inline bool StringUtil::strToUInt32(const std::string& str, uint32_t& v) {
    try {
        std::stringstream ss;
        ss << str;
        ss >> v;
        if (ss.fail()) return false;
        return true;
    } catch (std::exception& e) {
        std::cerr << "convert string[" << str << "] to uint32_t failed" << std::endl;
        return false;
    }
}

inline bool StringUtil::strToInt64(const std::string& str, int64_t& v) {
    try {
        std::stringstream ss;
        ss << str;
        ss >> v;
        if (ss.fail()) return false;
        return true;
    } catch (std::exception& e) {
        std::cerr << "convert string[" << str << "] to int64_t failed" << std::endl;
        return false;
    }
}

inline std::string StringUtil::int64ToStr(const int64_t v) {
    std::stringstream ss;
    ss << v;
    std::string str;
    ss >> str;
    return str;
}

inline std::string StringUtil::int32ToStr(const int32_t v) {
    std::stringstream ss;
    ss << v;
    std::string str;
    ss >> str;
    return str;
}

inline std::string StringUtil::uint32ToStr(const uint32_t v) {
    std::stringstream ss;
    ss << v;
    std::string str;
    ss >> str;
    return str;
}

inline void StringUtil::trim(std::string& str) {
    if (str.empty()) return;
    str.erase(0, str.find_first_not_of(" \t\r\n"));
    str.erase(str.find_last_not_of(" \t\r\n") + 1);
}

inline std::vector<std::string> StringUtil::split(const std::string& str, const std::string& splitStr) {
    std::vector<std::string> splitList;
    if (str.empty()) return splitList;
    size_t start = 0, end = std::string::npos;
    do {
        end = str.find(splitStr, start);
        if (end == std::string::npos) splitList.push_back(str.substr(start, end));
        else {
            splitList.push_back(str.substr(start, end - start));
            start = end + splitStr.size();
            if (start == str.size()) splitList.push_back("");
        }
    } while (end != std::string::npos && start < str.size());
    return splitList;
}

END_NAMESPACE(common)
END_NAMESPACE(knn)
END_NAMESPACE(manong)
END_NAMESPACE(xin)

#endif

