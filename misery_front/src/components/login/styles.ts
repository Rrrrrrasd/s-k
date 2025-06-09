import styled from 'styled-components'

export const LoginContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
`;

export const LoginForm = styled.form`
  width: 360px;
  padding: 3rem 2rem;
  border-radius: 1rem;
  box-shadow: 0 10px 25px rgba(92, 99, 105, 0.2);

  &__title {
    font-weight: 500;
    margin-bottom: 2.5rem;
  }

  &__div {
    position: relative;
    height: 52px;
    margin-bottom: 1.5rem;
  }

  &__input {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    font-size: 1rem;
    border: 2px solid #DADCE0;
    border-radius: 0.5rem;
    outline: none;
    padding: 1rem;
    background: none;
    z-index: 1;

    &:focus + label {
      top: -0.5rem;
      left: 0.8rem;
      color: #1A73E8;
      font-size: 0.75rem;
      font-weight: 500;
      z-index: 10;
    }

    &:not(:placeholder-shown).form__input:not(:focus) + label {
      top: -0.5rem;
      left: 0.8rem;
      z-index: 10;
      font-size: 0.75rem;
      font-weight: 500;
    }

    &:focus {
      border: 2px solid #1A73E8;
    }
  }

  &__label {
    position: absolute;
    left: 1rem;
    top: 1rem;
    padding: 0 0.25rem;
    background-color: #fff;
    color: #80868B;
    font-size: 1rem;
    transition: 0.3s;
  }

  &__button {
    display: block;
    margin-left: auto;
    padding: 0.75rem 2rem;
    outline: none;
    border: none;
    background-color: #1A73E8;
    color: #fff;
    font-size: 1rem;
    border-radius: 0.25rem;
    cursor: pointer;
    transition: 0.3s;

    &:hover {
      box-shadow: 0 10px 36px rgba(0, 0, 0, 0.15);
    }
  }
`;

export const FormTitle = styled.h1`
  font-weight: 500;
  margin-bottom: 2.5rem;
`;

export const FormDiv = styled.div`
  position: relative;
  height: 52px;
  margin-bottom: 1.5rem;
`;

export const FormInput = styled.input`
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  font-size: 1rem;
  border: 2px solid #DADCE0;
  border-radius: 0.5rem;
  outline: none;
  padding: 1rem;
  background: none;
  z-index: 1;
`;

export const FormLabel = styled.label`
  position: absolute;
  left: 1rem;
  top: 1rem;
  padding: 0 0.25rem;
  background-color: #fff;
  color: #80868B;
  font-size: 1rem;
  transition: 0.3s;
`;

export const ButtonContainer = styled.div`
  display: flex;
  flex-direction: row-reverse; /* 버튼 순서를 오른쪽에서 왼쪽으로 */
  gap: 10px; /* 버튼 사이 간격 */
  margin-top: 20px; /* 버튼 그룹 상단 마진 */
`;

export const FormButton = styled.input`
  display: block;
  margin-left: auto;
  padding: 0.75rem 2rem;
  outline: none;
  border: none;
  background-color: #1A73E8;
  color: #fff;
  font-size: 1rem;
  border-radius: 0.25rem;
  cursor: pointer;
  transition: 0.3s;
`;