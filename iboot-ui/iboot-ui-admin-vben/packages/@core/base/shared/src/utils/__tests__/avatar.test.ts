import { describe, expect, it } from 'vitest';

import { getAvatarInitials } from '../avatar';

describe('getAvatarInitials', () => {
  it('should take the last two characters for CJK names', () => {
    expect(getAvatarInitials('张伟')).toBe('张伟');
    expect(getAvatarInitials('王小明')).toBe('小明');
    expect(getAvatarInitials('李')).toBe('李');
  });

  it('should take the first letter in uppercase for latin names', () => {
    expect(getAvatarInitials('admin')).toBe('A');
    expect(getAvatarInitials('jack')).toBe('J');
    expect(getAvatarInitials('Admin107')).toBe('A');
  });

  it('should take the first letter of the first two words when spaced', () => {
    expect(getAvatarInitials('jack ma')).toBe('JM');
    expect(getAvatarInitials('john ronald reuel tolkien')).toBe('JR');
    expect(getAvatarInitials('张 三')).toBe('张三');
  });

  it('should handle numeric names', () => {
    expect(getAvatarInitials('123456')).toBe('1');
  });

  it('should fall back to a question mark for empty values', () => {
    expect(getAvatarInitials('')).toBe('?');
    expect(getAvatarInitials('   ')).toBe('?');
    expect(getAvatarInitials()).toBe('?');
    expect(getAvatarInitials(null)).toBe('?');
  });

  it('should split by code point so emoji stay intact', () => {
    expect(getAvatarInitials('小明🙂')).toBe('明🙂');
    expect(getAvatarInitials('🙂')).toBe('🙂');
  });

  it('should trim surrounding whitespace', () => {
    expect(getAvatarInitials('  admin  ')).toBe('A');
    expect(getAvatarInitials('  张伟  ')).toBe('张伟');
  });
});
